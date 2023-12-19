package my.dictionary.free.view.quiz.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import my.dictionary.free.domain.models.quiz.Quiz

class QuizDetailTabsAdapter(fragment: Fragment, private val quiz: Quiz): FragmentStateAdapter(fragment) {

    companion object {
        private const val PAGES = 2
    }

    override fun getItemCount(): Int = PAGES

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> {
                QuizDetailFragment().also {
                    it.arguments = Bundle().apply {
                        putParcelable(QuizDetailFragment.BUNDLE_QUIZ, quiz)
                    }
                }
            }
            else -> {
                QuizHistoryFragment().also {
                    it.arguments = Bundle().apply {
                        putParcelable(QuizHistoryFragment.BUNDLE_QUIZ, quiz)
                    }
                }
            }
        }
    }
}