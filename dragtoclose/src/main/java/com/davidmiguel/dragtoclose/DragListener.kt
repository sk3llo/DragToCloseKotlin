package com.davidmiguel.dragtoclose

/**
 *
 * Enables to listen drag events.
 *
 */

interface DragListener {


    /**
     *
     * Invoked when the view has just started to be dragged.
     *
     */

    fun onStartDraggingView()


    /**
     *
     * Invoked when the view has being dragged out of the screen
     *
     * and just before calling activity.finish().
     *
     */

    fun onViewClosed()

}