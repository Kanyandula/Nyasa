package com.kanyandula.nyasa.ui.main.blog

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.kanyandula.nyasa.R
import com.kanyandula.nyasa.databinding.FragmentBlogBinding
import com.kanyandula.nyasa.models.BlogPost
import com.kanyandula.nyasa.persistance.BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
import com.kanyandula.nyasa.persistance.BlogQueryUtils.BLOG_FILTER_USERNAME
import com.kanyandula.nyasa.persistance.BlogQueryUtils.BLOG_ORDER_ASC
import com.kanyandula.nyasa.util.TopSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BlogFragment :
    BaseBlogFragment<FragmentBlogBinding>(FragmentBlogBinding::inflate),
    BlogListAdapter.Interaction,
    SwipeRefreshLayout.OnRefreshListener {

    private lateinit var searchView: SearchView
    private lateinit var recyclerAdapter: BlogListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setupMenu()
        binding?.swipeRefresh?.setOnRefreshListener(this)
        initRecyclerView()
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pagingDataFlow.collectLatest { pagingData ->
                    recyclerAdapter.submitData(pagingData)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                recyclerAdapter.loadStateFlow.collectLatest { loadStates ->
                    binding?.swipeRefresh?.isRefreshing =
                        loadStates.refresh is LoadState.Loading
                }
            }
        }
    }

    private fun initSearchView(menu: Menu) {
        activity?.apply {
            val searchManager: SearchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(R.id.action_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.maxWidth = Integer.MAX_VALUE
            searchView.setIconifiedByDefault(true)
            searchView.isSubmitButtonEnabled = true
        }

        val searchPlate = searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        searchPlate.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED ||
                actionId == EditorInfo.IME_ACTION_SEARCH
            ) {
                val searchQuery = v.text.toString()
                Log.e(TAG, "SearchView: (keyboard or arrow) executing search...: $searchQuery")
                viewModel.setQuery(searchQuery)
                onBlogSearchOrFilter()
            }
            true
        }

        val searchButton = searchView.findViewById(androidx.appcompat.R.id.search_go_btn) as View
        searchButton.setOnClickListener {
            val searchQuery = searchPlate.text.toString()
            Log.e(TAG, "SearchView: (button) executing search...: $searchQuery")
            viewModel.setQuery(searchQuery)
            onBlogSearchOrFilter()
        }
    }

    private fun onBlogSearchOrFilter() {
        viewModel.executeSearch()
        resetUI()
    }

    private fun resetUI() {
        binding?.blogPostRecyclerview?.smoothScrollToPosition(0)
        stateChangeListener.hideSoftKeyboard()
        binding?.focusableView?.requestFocus()
    }

    private fun initRecyclerView() {
        binding?.blogPostRecyclerview?.apply {
            layoutManager = LinearLayoutManager(this@BlogFragment.context)
            if (itemDecorationCount == 0) {
                addItemDecoration(TopSpacingItemDecoration(30))
            }
            recyclerAdapter = BlogListAdapter(requestManager, this@BlogFragment)
            adapter = recyclerAdapter.withLoadStateFooter(
                footer = BlogLoadStateAdapter(retry = { recyclerAdapter.retry() })
            )
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(
            object : MenuProvider {
                override fun onPrepareMenu(menu: Menu) { /* no-op */ }

                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.search_menu, menu)
                    initSearchView(menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.action_filter_settings -> {
                            showFilterDialog()
                            return true
                        }
                    }
                    return true
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    override fun onItemSelected(position: Int, item: BlogPost) {
        val action = BlogFragmentDirections.actionBlogFragmentToViewBlogFragment(item.slug)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.blogPostRecyclerview?.adapter = null
    }

    override fun onRefresh() {
        recyclerAdapter.refresh()
    }

    @Suppress("LongMethod")
    fun showFilterDialog() {
        activity?.let {
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_blog_filter)

            val view = dialog.getCustomView()

            val filter = viewModel.getFilter()
            val order = viewModel.getOrder()

            if (filter.equals(BLOG_FILTER_DATE_UPDATED)) {
                view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_date)
            } else {
                view.findViewById<RadioGroup>(R.id.filter_group).check(R.id.filter_author)
            }

            if (order.equals(BLOG_ORDER_ASC)) {
                view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_asc)
            } else {
                view.findViewById<RadioGroup>(R.id.order_group).check(R.id.filter_desc)
            }

            view.findViewById<TextView>(R.id.positive_button).setOnClickListener {
                Log.d(TAG, "FilterDialog: apply filter.")

                val selectedFilterId = dialog.getCustomView()
                    .findViewById<RadioGroup>(R.id.filter_group).checkedRadioButtonId
                val selectedOrderId = dialog.getCustomView()
                    .findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId

                val newFilter = if (selectedFilterId == R.id.filter_author) {
                    BLOG_FILTER_USERNAME
                } else {
                    BLOG_FILTER_DATE_UPDATED
                }

                val newOrder = if (selectedOrderId == R.id.filter_desc) "-" else ""
                viewModel.saveFilterOptions(newFilter, newOrder)
                viewModel.setBlogFilter(newFilter)
                viewModel.setBlogOrder(newOrder)
                onBlogSearchOrFilter()
                dialog.dismiss()
            }

            view.findViewById<TextView>(R.id.negative_button).setOnClickListener {
                Log.d(TAG, "FilterDialog: cancelling filter.")
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}
