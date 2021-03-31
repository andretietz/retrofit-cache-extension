package com.andretietz.retrofitcache

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.andretietz.retrofitcache.MainViewModel.ViewState.*
import com.andretietz.retrofitcache.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    viewModel.state.flowWithLifecycle(this.lifecycle, Lifecycle.State.STARTED)
      .onEach {

        when (it) {
          is InitialState -> {
            binding.textNumber.text = ""
            binding.textWhereFrom.text = getText(R.string.main_default_description)
          }
          is NumberUpdate -> {
            binding.refresh.isRefreshing = false
            binding.textNumber.text = "${it.value}"
            binding.textWhereFrom.text = getText(
              if (it.fromCache) {
                R.string.main_cachehit_description
              } else {
                R.string.main_cachemiss_description
              }
            ).toString().format(it.validityMillis)

          }
          is Error -> {
            binding.refresh.isRefreshing = false
            binding.textNumber.text = "-1"
            binding.textWhereFrom.text = "${it.error.message}"
          }
        }
      }
      .launchIn(lifecycleScope)

    binding.refresh.setOnRefreshListener {
      lifecycleScope.launch { viewModel.updateNumber() }
    }
  }
}
