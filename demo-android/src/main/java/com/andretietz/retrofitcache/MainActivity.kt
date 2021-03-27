package com.andretietz.retrofitcache

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.andretietz.retrofitcache.MainViewModel.ViewState.*
import com.andretietz.retrofitcache.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    lifecycleScope.launch {
      viewModel.state.consumeAsFlow().collect {
        when (it) {
          is InitialState -> {
            binding.textNumber.text = ""
            binding.textWhereFrom.text = getText(R.string.main_default_description)
          }
          is NumberUpdate -> {
            binding.textNumber.text = "${it.value}"
            binding.textWhereFrom.text = getText(
              if (it.fromCache) {
                R.string.main_cachehit_description
              } else {
                R.string.main_cachemiss_description
              }
            )

          }
          is Error -> {
            binding.textNumber.text = "-1"
            binding.textWhereFrom.text = "${it.error.message}"
          }
        }
      }
    }

    binding.refresh.setOnRefreshListener {
      lifecycleScope.launch {
        viewModel.updateNumber()
        binding.refresh.isRefreshing = false
      }
    }
  }
}
