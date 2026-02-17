package com.harsh.noteitform.form.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.harsh.noteitform.data.model.Type
import com.harsh.noteitform.form.FormActivity
import com.harsh.noteitform.form.FormFragment

class FormPagerAdapter (
    activity: FormActivity,
    private val types: List<Type>
    ) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int = types.size

        override fun createFragment(position: Int): Fragment {
            val type = types[position]
            return FormFragment.newInstance(
                typeId = type.id,
                feedbackTypeId = type.feedbackTypeId
            )
        }
    }