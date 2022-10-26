package com.kanyandula.nyasa.ui.main.create_blog

import android.os.Bundle
import android.view.View
import com.kanyandula.nyasa.databinding.FragmentCreateBlogBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi


@OptIn(ExperimentalCoroutinesApi::class)
@AndroidEntryPoint
class CreateBlogFragment : BaseCreateBlogFragment<FragmentCreateBlogBinding>(FragmentCreateBlogBinding::inflate){



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}