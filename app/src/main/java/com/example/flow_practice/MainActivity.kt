package com.example.flow_practice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flow_practice.ui.theme.FlowpracticeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = viewModel<MainViewModel>()
            // flow의 값을 state로 사용하기 위하여, collectAsState() 사용
            // viewmodel에서 emit될 때마다, UI 레이어에서 time은 변경에 대한 notification을 받고 새로운 값으로 반영이 된다.
            val time = viewModel.countDownFlow.collectAsState(initial = 10)
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = time.value.toString(),
                    fontSize = 30.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

