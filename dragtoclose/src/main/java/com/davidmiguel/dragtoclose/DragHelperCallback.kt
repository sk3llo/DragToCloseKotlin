package com.davidmiguel.dragtoclose

import android.support.v4.widget.ViewDragHelper
import android.util.Log

import android.view.View
import com.davidmiguel.dragtoclose.DragToClose.Companion.HEIGHT_THRESHOLD_TO_CLOSE
import com.davidmiguel.dragtoclose.DragToClose.Companion.SPEED_THRESHOLD_TO_CLOSE_DOWN
import com.davidmiguel.dragtoclose.DragToClose.Companion.SPEED_THRESHOLD_TO_CLOSE_UP
import com.davidmiguel.dragtoclose.DragToClose.Companion.dragDirection


//import com.topset.com.topset.dragtoclose.DragToClose.HEIGHT_THRESHOLD_TO_CLOSE
//
//import com.topset.com.topset.dragtoclose.DragToClose.SPEED_THRESHOLD_TO_CLOSE


/**
 *
 * Dragging controller.
 *
 */

internal class DragHelperCallback(private val dragToClose: DragToClose
                                  , private val draggableContainer: View) : ViewDragHelper.Callback() {


    private var lastDraggingState: Int = 0

    private var topBorderDraggableContainer: Int = 0

    private var dd = dragDirection?.toInt()


    init {

        lastDraggingState = ViewDragHelper.STATE_IDLE

    }


//    /**
//     *
//     * Checks dragging states and notifies them.
//     *
//     */

    override fun onViewDragStateChanged(state: Int) {


        // If no state change, don't do anything
        if (state == lastDraggingState) {

            return

        }

        // If last state was dragging or settling and current state is idle,

        // the view has stopped moving. If the top border of the container is

        // equal to the vertical draggable range, the view has being dragged out,

        // so close activity is called


        when (dd){
            //Up
            1 -> {

                if ((lastDraggingState == ViewDragHelper.STATE_DRAGGING
                                || lastDraggingState == ViewDragHelper.STATE_SETTLING)
                        && state == ViewDragHelper.STATE_IDLE
                        && (Math.abs(topBorderDraggableContainer) == Math.abs(dragToClose.draggableRange))
                ) {
                    dragToClose.closeActivity()
                }
            }
            //Down
            2 -> {
                if ((lastDraggingState == ViewDragHelper.STATE_DRAGGING
                                || lastDraggingState == ViewDragHelper.STATE_SETTLING)
                        && state == ViewDragHelper.STATE_IDLE
                        && (topBorderDraggableContainer == dragToClose.draggableRange)
                ) {
                    dragToClose.closeActivity()
                }
            }
            //Up and Down
            3 -> {
                if ((lastDraggingState == ViewDragHelper.STATE_DRAGGING
                                || lastDraggingState == ViewDragHelper.STATE_SETTLING)
                        && state == ViewDragHelper.STATE_IDLE
                        && (topBorderDraggableContainer == dragToClose.draggableRange
                                || topBorderDraggableContainer == -dragToClose.draggableRange)
                ) {
                    dragToClose.closeActivity()
                }
            }
        }

//        if ((lastDraggingState == ViewDragHelper.STATE_DRAGGING
//                        || lastDraggingState == ViewDragHelper.STATE_SETTLING)
//
//                && state == ViewDragHelper.STATE_IDLE
//
//                && (topBorderDraggableContainer == dragToClose.draggableRange
//                || topBorderDraggableContainer == -dragToClose.draggableRange)
//                ) {
//
//            dragToClose.closeActivity()
//
//
//        }

        // If the view has just started being dragged, notify event

        if (state == ViewDragHelper.STATE_DRAGGING) {

            dragToClose.onStartDraggingView()

        }

        // Save current state

        lastDraggingState = state

    }


    /**
     *
     * Registers draggable container position and changes the transparency of the container
     *
     * based on the vertical position while the view is being vertical dragged.
     *
     */

    override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {

        topBorderDraggableContainer = top

        dragToClose.changeDragViewViewAlpha()

    }


    /**
     *
     * Handles the settling of the draggable view when it is released.
     *
     * Dragging speed is more important than the place the view is released.
     *
     * If the speed is greater than SPEED_THRESHOLD_TO_CLOSE the view is settled to closed.
     *
     * Else if the top
     *
     */

    override fun onViewReleased(releasedChild: View, xVel: Float, yVel: Float) {

        // If view is in its original position or out of range, don't do anything
        if (topBorderDraggableContainer == 0
                || topBorderDraggableContainer >= dragToClose.draggableRange
        ) {

            return

        }

        var settleToClosedDown = false
        var settleToClosedUp = false

        // Check speed

        if (yVel > SPEED_THRESHOLD_TO_CLOSE_DOWN ) {
            settleToClosedDown = true

        }else if (yVel < SPEED_THRESHOLD_TO_CLOSE_UP){
            settleToClosedUp = true
        }
        else {

            // Check position

            val verticalDraggableDown = (dragToClose.draggableRange * HEIGHT_THRESHOLD_TO_CLOSE)
            val verticalDraggableUp = (dragToClose.draggableRange * HEIGHT_THRESHOLD_TO_CLOSE)

            //Swipe down was detected
            if (topBorderDraggableContainer > verticalDraggableDown) {

                settleToClosedDown = true

                //Swipe top was detected
            } else if (Math.abs(topBorderDraggableContainer) > Math.abs(verticalDraggableUp)){

                settleToClosedUp = true
            }

        }

//         If dragged more than HEIGHT_THRESHOLD_TO_CLOSE% of the screen-> moved view out of the screen

        when {
            settleToClosedDown -> {
                dragToClose.smoothScrollToY(dragToClose.draggableRange)
//                return
            }
            settleToClosedUp -> {
                dragToClose.smoothScrollToY(-dragToClose.draggableRange)
//                return

            }
            else -> dragToClose.smoothScrollToY(0)
        }

    }


    /**
     *
     * Sets the vertical draggable range.
     *
     */

    override fun getViewVerticalDragRange(child: View): Int {
        return dragToClose.draggableRange

    }


    /**
     *
     * Configures which is going to be the draggable container.
     *
     */

    override fun tryCaptureView(child: View, pointerId: Int): Boolean {

        return child == draggableContainer

    }


    /**
     *
     * Defines clamped position for left border.
     *
     * DragToClose padding must be taken into consideration.
     *
     */

    override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {

        return child.left

    }


    /**
     *
     * Defines clamped position for top border.
     *
     */

    override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {

        var topBound = 0
        var bottomBound = 0
        //Up swipe is chosen -> adjust the clamped position
        when (dd) {
            1 -> {
                topBound =  - dragToClose.draggableRange
                bottomBound = - dragToClose.paddingTop
                Log.e("DHC::::::", "top: $topBound, btm:  $bottomBound")
                //Down swipe is chosen -> adjust the clamped position
            }
            2 -> {
                topBound = dragToClose.paddingTop
                bottomBound = dragToClose.draggableRange
            }
            else -> {
                //UpAndDown swipe is chosen -> adjust the clamped position
                topBound = -dragToClose.draggableRange // Top limit
                bottomBound = dragToClose.draggableRange
            }
        }

        return Math.min(Math.max(top, topBound), bottomBound)

    }

}