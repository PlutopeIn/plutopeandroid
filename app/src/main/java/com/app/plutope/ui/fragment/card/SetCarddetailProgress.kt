package com.app.plutope.ui.fragment.card

import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.app.plutope.R
import com.app.plutope.ui.base.BaseActivity
import de.hdodenhof.circleimageview.CircleImageView


fun setCardProgress(view: View, progress: Int, context: BaseActivity) {


    val imgCircle1: CircleImageView = view.findViewById(R.id.img_circle_1)
    val imgCircle2: CircleImageView = view.findViewById(R.id.img_circle_2)
    val imgCircle3: CircleImageView = view.findViewById(R.id.img_circle_3)
    val imgCircle4: CircleImageView = view.findViewById(R.id.img_circle_4)

    val view1: View = view.findViewById(R.id.view_1)
    val view2: View = view.findViewById(R.id.view_2)
    val view3: View = view.findViewById(R.id.view_3)
    val view4: View = view.findViewById(R.id.view_4)

    val view6: View = view.findViewById(R.id.view_6)
    val view7: View = view.findViewById(R.id.view_7)

    val personalDetail: TextView = view.findViewById(R.id.txt_personal_details)
    val membership: TextView = view.findViewById(R.id.txt_membership)
    val card: TextView = view.findViewById(R.id.txt_card)
    val payment: TextView = view.findViewById(R.id.txt_payment)

    when (progress) {

        1 -> {
            imgCircle1.background = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.background_circle_progress,
                null
            )

            view1.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_25264D,
                    null
                )
            )
            personalDetail.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.white,
                    null
                )
            )


            imgCircle2.background = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.background_circle_non_progress,
                null
            )
            view2.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_25264D,
                    null
                )
            )
            view4.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_25264D,
                    null
                )
            )
            membership.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_7576D,
                    null
                )
            )



            imgCircle3.background = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.background_circle_non_progress,
                null
            )
            view3.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_25264D,
                    null
                )
            )
            card.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_7576D,
                    null
                )
            )


        }

        2 -> {

            imgCircle1.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_progress,
                    null
                )

            view1.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            personalDetail.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.white,
                    null
                )
            )

            imgCircle2.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_progress,
                    null
                )
            view2.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            view4.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_25264D,
                    null
                )
            )
            membership.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.white,
                    null
                )
            )

            imgCircle3.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_non_progress,
                    null
                )

            view3.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_25264D,
                    null
                )
            )
/*
            personalDetail.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.purple_7576D,
                    null
                )
            )
*/

        }

        3 -> {
            imgCircle1.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_progress,
                    null
                )

            view1.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            personalDetail.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.white,
                    null
                )
            )


            imgCircle2.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_progress,
                    null
                )

            view2.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            view4.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            membership.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.white,
                    null
                )
            )


            imgCircle3.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_progress,
                    null
                )

            view3.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            card.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))


        }

        4 -> {
            imgCircle1.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_progress,
                    null
                )

            view1.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            personalDetail.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.white,
                    null
                )
            )


            imgCircle2.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_progress,
                    null
                )

            view2.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            view4.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            membership.setTextColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.white,
                    null
                )
            )


            imgCircle3.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_progress,
                    null
                )

            view3.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )
            card.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))

            imgCircle4.background =
                ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.background_circle_progress,
                    null
                )

            view6.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )

            view7.setBackgroundColor(
                ResourcesCompat.getColor(
                    context.resources,
                    R.color.blue_00C6FB,
                    null
                )
            )


            payment.setTextColor(ResourcesCompat.getColor(context.resources, R.color.white, null))


        }

    }

}
