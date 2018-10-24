package com.topset.dragtoclosekotlin

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.davidmiguel.dragtoclose.DragToClose

class Down: Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inf = inflater.inflate(R.layout.down_fragment, container,false)
        var v = inf.findViewById<DragToClose>(R.id.dragToCloseFragment)
        v.bringToFront()
        return inf
    }

    override fun onDestroyView() {
        this.activity?.findViewById<Button>(R.id.up)?.visibility = View.VISIBLE
        this.activity?.findViewById<Button>(R.id.down)?.visibility = View.VISIBLE
        this.activity?.findViewById<Button>(R.id.upAndDown)?.visibility = View.VISIBLE
        super.onDestroyView()
    }
}