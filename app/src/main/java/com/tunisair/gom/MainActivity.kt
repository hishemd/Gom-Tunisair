package com.tunisair.gom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import com.tunisair.gom.ui.GomViewModel
import com.tunisair.gom.ui.screens.GomApp
import com.tunisair.gom.ui.theme.GomTheme
import com.tunisair.gom.util.BookmarkStore

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: GomViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val store = BookmarkStore(filesDir.resolve("bookmarks.txt"))
        viewModel = GomViewModel(store)

        setContent {
            GomTheme {
                BackHandler(enabled = true) {
                    if (!viewModel.back()) finish()
                }
                GomApp(viewModel)
            }
        }
    }
}
