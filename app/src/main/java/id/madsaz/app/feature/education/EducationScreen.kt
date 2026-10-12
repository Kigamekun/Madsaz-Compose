package id.madsaz.app.feature.education

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.madsaz.app.R
import id.madsaz.app.ui.components.CompactContainer
import id.madsaz.app.ui.theme.AppBackground
import id.madsaz.app.ui.theme.Border
import id.madsaz.app.ui.theme.BrandBlue
import id.madsaz.app.ui.theme.Ink
import id.madsaz.app.ui.theme.MutedInk

private data class Article(
    val id: String,
    val title: String,
    val excerpt: String,
    val author: String,
    val category: String,
    val image: Int,
)

private val articles = listOf(
    Article("suara-bayi", "Tips Memahami 5 Suara Bayi", "Ayo kenali arti Neh, Eh, Heh, Eairh, dan Owh. Untuk memahami anak", "dr. Andini Putri", "Suara Bayi", R.drawable.education_cry),
    Article("tidur-bayi", "Tips Menciptakan Tidur Bayi yang Nyaman", "Lingkungan, rutinitas, dan posisi tidur yang aman.", "Bidan Sari Dewi", "Tidur", R.drawable.education_sleep),
    Article("bayi-lapar", "Tanda Bayi Lapar dan Cara Meresponsnya", "Kenali sinyal awal bayi membutuhkan ASI atau susu.", "dr. Raka Pratama", "Menyusu", R.drawable.education_hunger),
)

@Composable
fun EducationListScreen(onBack: () -> Unit, onArticle: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Semua") }
    val visibleArticles = articles.filter {
        (category == "Semua" || it.category == category) &&
            (query.isBlank() || it.title.contains(query, ignoreCase = true) || it.excerpt.contains(query, ignoreCase = true))
    }

    CompactContainer {
        Box(Modifier.fillMaxSize().background(AppBackground)) {
            Image(
                painterResource(R.drawable.education_hero), null,
                Modifier.fillMaxWidth().height(214.dp), contentScale = ContentScale.Crop,
            )
            LazyColumn(Modifier.fillMaxSize()) {
                item { Spacer(Modifier.height(190.dp)) }
                item {
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .background(AppBackground).padding(top = 24.dp, bottom = 44.dp),
                    ) {
                        Text("Edukasi Parenting", Modifier.padding(horizontal = 20.dp), color = Ink,
                            style = MaterialTheme.typography.headlineMedium, fontSize = 24.sp)
                        Text(
                            "Artikel dan video pilihan dari dokter anak, bidan, dan ahli untuk mendukung perjalanan parenting Anda.",
                            Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp),
                            color = MutedInk, fontSize = 12.sp, lineHeight = 17.sp,
                        )
                        Surface(
                            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 28.dp).height(42.dp),
                            RoundedCornerShape(16.dp), Color.White, border = BorderStroke(1.dp, Border),
                        ) {
                            Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Search, null, Modifier.size(18.dp), tint = MutedInk)
                                BasicTextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodySmall.copy(color = Ink),
                                    decorationBox = { inner ->
                                        Box {
                                            if (query.isEmpty()) Text("Cari topik edukasi...", color = MutedInk, fontSize = 12.sp)
                                            inner()
                                        }
                                    },
                                )
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                                .padding(start = 20.dp, end = 20.dp, top = 18.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            listOf("Semua", "Suara Bayi", "Tidur", "Menyusu", "Tumbuh").forEach { option ->
                                Surface(
                                    onClick = { category = option },
                                    shape = RoundedCornerShape(13.dp),
                                    color = if (category == option) BrandBlue else Color.White,
                                    border = if (category == option) null else BorderStroke(1.dp, Border),
                                ) {
                                    Text(option, Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
                                        color = if (category == option) Color.White else BrandBlue,
                                        fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            if (visibleArticles.isEmpty()) {
                                Text("Belum ada artikel untuk topik ini.", Modifier.padding(16.dp), color = MutedInk)
                            } else visibleArticles.forEach { article ->
                                Surface(
                                    onClick = { onArticle(article.id) },
                                    modifier = Modifier.fillMaxWidth().height(118.dp),
                                    shape = RoundedCornerShape(18.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Border),
                                ) {
                                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            painterResource(article.image), null,
                                            Modifier.width(126.dp).height(94.dp).clip(RoundedCornerShape(14.dp)),
                                            contentScale = ContentScale.Crop,
                                        )
                                        Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(article.title, color = Ink, fontSize = 12.sp, lineHeight = 15.sp,
                                                fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text(article.excerpt, color = MutedInk, fontSize = 9.5.sp, lineHeight = 12.sp,
                                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text(article.author, color = Ink, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            EducationBackButton(onBack, Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 28.dp))
        }
    }
}

@Composable
fun EducationDetailScreen(articleId: String, onBack: () -> Unit) {
    val article = articles.firstOrNull { it.id == articleId } ?: return
    val isSleepArticle = article.id == "tidur-bayi"
    CompactContainer {
        Box(Modifier.fillMaxSize().background(Color.White)) {
            Image(
                painterResource(if (isSleepArticle) R.drawable.education_sleep_cover else article.image), null,
                Modifier.fillMaxWidth().height(282.dp), contentScale = ContentScale.Crop,
            )
            LazyColumn(Modifier.fillMaxSize()) {
                item { Spacer(Modifier.height(256.dp)) }
                item {
                    Column(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .background(Color.White).padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(article.title, color = Ink, style = MaterialTheme.typography.headlineMedium,
                            fontSize = 21.sp, lineHeight = 25.sp)
                        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFE6F4FB)) {
                            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Ringkasan", color = Ink, style = MaterialTheme.typography.titleMedium, fontSize = 14.sp)
                                Text(
                                    if (isSleepArticle) "Tidur berkualitas penting untuk tumbuh kembang bayi. Artikel ini membahas rutinitas, lingkungan, dan kebiasaan yang membantu bayi tidur lebih nyaman." else article.excerpt,
                                    color = MutedInk, fontSize = 10.sp, lineHeight = 14.sp,
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(Modifier.size(34.dp), CircleShape, Color(0xFFE6F4FB)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(article.author.first().toString(), color = BrandBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Column(Modifier.padding(start = 10.dp)) {
                                Text(article.author, color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                if (isSleepArticle) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("Bidan Praktik Mandiri", color = MutedInk, fontSize = 9.5.sp)
                                        Text("12 Sep 2024", color = MutedInk, fontSize = 9.5.sp)
                                    }
                                }
                            }
                        }
                        Text("Isi Artikel", color = Ink, style = MaterialTheme.typography.titleLarge, fontSize = 16.sp)
                        Text(
                            if (isSleepArticle) "1. Kenali Tanda-Tanda Bayi Mengantuk" else article.title,
                            color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        )
                        Text(
                            if (isSleepArticle) "Bayi biasanya mulai menunjukkan tanda-tanda mengantuk seperti mengucek mata, rewel, atau menghindari kontak mata. Segera respons tanda-tanda ini agar bayi tidak terlalu lelah." else article.excerpt,
                            color = MutedInk, fontSize = 10.sp, lineHeight = 14.sp,
                        )
                        Image(
                            painterResource(if (isSleepArticle) R.drawable.education_sleep_inline else article.image), null,
                            Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
            EducationBackButton(onBack, Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 28.dp))
        }
    }
}

@Composable
private fun EducationBackButton(onBack: () -> Unit, modifier: Modifier) {
    Surface(modifier.size(40.dp), CircleShape, Color.White) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Ink)
        }
    }
}
