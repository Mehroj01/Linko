package com.neb.linko.ui.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.neb.linko.cache.LanguageCache
import com.neb.linko.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    lateinit var aboutBinding: ActivityAboutBinding
    lateinit var languageCache: LanguageCache
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aboutBinding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(aboutBinding.root)

        languageCache = LanguageCache(getSharedPreferences("Base", MODE_PRIVATE))

        aboutBinding.title.text = if (languageCache.getLanguage()) "About us" else "معلومات عنا"

        aboutBinding.backBtn.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!languageCache.getLanguage()){
            aboutBinding.longText.text = "غرضنا\n" +
                    "إطلاق العنان لقوة الطعام لتحسين نوعية الحياة للجميع ، اليوم وللأجيال القادمة. هذا هو هدفنا.\n" +
                    "\n" +
                    "نحن شركة الغذاء الجيد ، الحياة الجيدة. نحن نؤمن بقوة الغذاء لتحسين الحياة. الغذاء الجيد يغذي ويسعد الحواس. يساعد الأطفال على النمو بصحة جيدة ، والحيوانات الأليفة تزدهر ، ويشيخ الآباء برشاقة ويعيش الجميع الحياة على أكمل وجه. الطعام الجيد يجمعنا. يحترم الطعام الجيد أيضًا كوكبنا ويحمي الموارد للأجيال القادمة.\n" +
                    "\n" +
                    "في نستله ، نقوم باستمرار باستكشاف ودفع حدود ما هو ممكن مع الأطعمة والمشروبات وحلول الصحة الغذائية لتحسين نوعية الحياة والمساهمة في مستقبل أكثر صحة. نحن نركز طاقتنا ومواردنا حيث يمكن لإطلاق قوة الغذاء أن يحدث فرقًا كبيرًا في حياة الناس والحيوانات الأليفة ، وحماية البيئة وتعزيزها ، وتوليد قيمة كبيرة لمساهمينا وأصحاب المصلحة على حد سواء.\n" +
                    "\n" +
                    "علاماتنا التجارية\n" +
                    "لدينا أكثر من 2000 علامة تجارية تتراوح من الرموز العالمية إلى المفضلة المحلية ، ونتواجد في 187 دولة حول العالم.\n" +
                    "\n" +
                    "طموحاتنا\n" +
                    "لقد حددنا ثلاثة طموحات شاملة لعام 2030 والتي توجه عملنا وتدعم أهداف الأمم المتحدة للتنمية المستدامة."
        }
    }
}