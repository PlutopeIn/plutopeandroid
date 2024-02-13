package com.app.plutope.utils.extras;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Pair;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
 */
public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;
    public static final int REQUEST_AUTHORIZATION = 1001;

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<String> createFile() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType("text/plain")
                    .setName("Untitled file");

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
     * contents.
     */
    public Task<Pair<String, String>> readFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File metadata = mDriveService.files().get(fileId).execute();
            String name = metadata.getName();

            // Stream the file contents to a String.
            try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();

                return Pair.create(name, contents);
            }
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code
     * content}.
     */
    public Task<Void> saveFile(String fileId, String name, String content) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.
            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, new Callable<FileList>() {
            @Override
            public FileList call() throws Exception {
                return mDriveService.files().list().setSpaces("drive").execute();
            }
        });
    }

    /**
     * Returns an {@link Intent} for opening the Storage Access Framework file picker.
     */
    public Intent createFilePickerIntent() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");

        return intent;
    }

    /**
     * Opens the file at the {@code uri} returned by a Storage Access Framework {@link Intent}
     * created by {@link #createFilePickerIntent()} using the given {@code contentResolver}.
     */
    public Task<Pair<String, String>> openFileUsingStorageAccessFramework(
            ContentResolver contentResolver, Uri uri) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the document's display name from its metadata.
            String name;
            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    name = cursor.getString(nameIndex);
                } else {
                    throw new IOException("Empty cursor returned for file.");
                }
            }

            // Read the document's contents as a String.
            String content;
            try (InputStream is = contentResolver.openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                content = stringBuilder.toString();
            }

            return Pair.create(name, content);
        });
    }

    public Task<String> createFile(String parentFolderId, String fileName, String mimeType, String content) {
        return Tasks.call(mExecutor, () -> {
            // Check if the file already exists in the parent folder
            FileList fileList = mDriveService.files().list()
                    .setQ("'" + parentFolderId + "' in parents and name = '" + fileName + "'")
                    .execute();

            if (fileList.getFiles().size() > 0) {
                // File already exists, return its ID
                String fileId = fileList.getFiles().get(0).getId();
                return fileId;
            } else {
                // File does not exist, create and upload it
                File metadata = new File()
                        .setParents(Collections.singletonList(parentFolderId))
                        .setMimeType(mimeType)
                        .setName(fileName);

                ByteArrayContent contentStream = ByteArrayContent.fromString(mimeType, content);

                File googleFile = mDriveService.files().create(metadata, contentStream).execute();
                if (googleFile == null) {
                    throw new IOException("Null result when requesting file creation.");
                }

                String fileId = googleFile.getId();
                return fileId;
            }
        });
    }


    public Task<String> createFolder(String parentFolderId, String folderName) {
        return Tasks.call(mExecutor, () -> {
            // Check if the folder already exists in the parent folder
            FileList fileList = mDriveService.files().list()
                    .setQ("'" + parentFolderId + "' in parents and mimeType='application/vnd.google-apps.folder' and name='" + folderName + "'")
                    .execute();

            if (fileList.getFiles().size() > 0) {
                // Folder already exists, return its ID
                String folderId = fileList.getFiles().get(0).getId();
                return folderId;
            } else {
                // Folder does not exist, create it
                File metadata = new File()
                        .setParents(Collections.singletonList(parentFolderId))
                        .setMimeType("application/vnd.google-apps.folder")
                        .setName(folderName);

                File googleFolder;
                try {
                    googleFolder = mDriveService.files().create(metadata).execute();
                } catch (IOException e) {
                    throw new IOException("Error creating folder: " + e.getMessage());
                }

                if (googleFolder == null) {
                    throw new IOException("Null result when requesting folder creation.");
                }

                return googleFolder.getId();
            }
        });
    }


    public void getFolderList(Context context, GoogleSignInAccount account) {
        Drive googleDriveService = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                JacksonFactory.getDefaultInstance(),
                GoogleAccountCredential.usingOAuth2(
                        context, Collections.singletonList(DriveScopes.DRIVE_READONLY)
                ).setSelectedAccount(account.getAccount())
        ).build();

        String query = "'root' in parents and mimeType = 'application/vnd.google-apps.folder'";
        FileList fileList;
        try {
            fileList = googleDriveService.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name)")
                    .execute();

            List<File> folders = fileList.getFiles();

            for (com.google.api.services.drive.model.File folder : folders) {
                String folderId = folder.getId();
                String folderName = folder.getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Task<FileList> getFileListFromFolder(Context context, GoogleSignInAccount account, String folderId, UserRecoverableAuthListener authListener) {
        return Tasks.call(mExecutor, () -> {
            Drive googleDriveService = new Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    JacksonFactory.getDefaultInstance(),
                    GoogleAccountCredential.usingOAuth2(
                            context, Collections.singletonList(DriveScopes.DRIVE_READONLY)
                    ).setSelectedAccount(account.getAccount())
            ).build();

            String query = "'" + folderId + "' in parents";
            FileList fileList;

            try {
                fileList = googleDriveService.files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("files(id, name, createdTime)") // Include createdTime
                        .execute();


                return fileList;
            } catch (UserRecoverableAuthIOException e) {
                authListener.requestUserRecoverableAuth(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }




    public interface UserRecoverableAuthListener {
        void requestUserRecoverableAuth(Intent recoveryIntent, int requestCode);
    }

    public interface  ContentCallback {
        void onContentAvailable(String content);
        void onContentFailed();
    }

    public Task<Void> deleteFileById(Context context, GoogleSignInAccount account, String fileId,UserRecoverableAuthListener authListener) {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        context, Collections.singleton(DriveScopes.DRIVE))
                .setSelectedAccount(account.getAccount());

        Drive driveService = new Drive.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("PlutoPeApp")
                .build();

        return Tasks.call(mExecutor,() -> {
            try {
                driveService.files().delete(fileId).execute();
                return null;
            } catch (GoogleJsonResponseException e) {
                // Handle API error
                e.printStackTrace();
                throw e;
            } catch (UserRecoverableAuthIOException e) {
                authListener.requestUserRecoverableAuth(e.getIntent(), REQUEST_AUTHORIZATION);
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
            return null;
        });
    }

    public Void getFileContent(Context context,GoogleSignInAccount account, String fileId,final ContentCallback callback) {

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                        context, Collections.singleton(DriveScopes.DRIVE))
                .setSelectedAccount(account.getAccount());

        Drive driveService = new Drive.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("PlutoPeApp")
                .build();
        AtomicReference<String> mContent = new AtomicReference<>("");
        Callable<String> task = () -> {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                return outputStream.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        };

        Task<String> resultTask = Tasks.call(mExecutor, task);


        resultTask.addOnSuccessListener(content -> {
            if (content != null) {
                mContent.set(content);
                callback.onContentAvailable(mContent.toString());
            }
        }).addOnFailureListener(e -> {
            e.printStackTrace();

            callback.onContentFailed();
        });

        return null;
    }


    public Task<String> getFolderIdFromFolderName(String parentFolderId, String folderName) {
        return Tasks.call(mExecutor, () -> {
            // Check if the folder already exists in the parent folder
            FileList fileList = mDriveService.files().list()
                    .setQ("'" + parentFolderId + "' in parents and mimeType='application/vnd.google-apps.folder' and name='" + folderName + "'")
                    .execute();

            if (fileList.getFiles().size() > 0) {
                // Folder already exists, return its ID
                String folderId = fileList.getFiles().get(0).getId();
                return folderId;
            } else {
               return "";
            }
        });
    }

}