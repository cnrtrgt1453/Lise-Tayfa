package com.cotx.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cotx.app.ui.theme.PrimaryPurple
import com.cotx.app.ui.theme.SecondaryOrange
import kotlin.math.cos
import kotlin.math.sin

/**
 * Lise ve YKS derslerinin (Matematik, Kimya, Coğrafya, Tarih) sanatsal sembollerini
 * içeren estetik ve dinamik arka plan bileşeni.
 */
@Composable
fun AcademicBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    val baseGradient = if (isDark) {
        listOf(
            Color(0xFF0F0C20),
            Color(0xFF181530),
            Color(0xFF0A0814)
        )
    } else {
        listOf(
            Color(0xFFF3F0FF),
            Color(0xFFEBE6FF),
            Color(0xFFF8F6FF)
        )
    }

    val primaryAccent = PrimaryPurple
    val secondaryAccent = SecondaryOrange
    val mathColor = if (isDark) Color(0xFF64B5F6).copy(alpha = 0.22f) else Color(0xFF1976D2).copy(alpha = 0.20f)
    val chemColor = if (isDark) Color(0xFF81C784).copy(alpha = 0.22f) else Color(0xFF388E3C).copy(alpha = 0.20f)
    val geoColor = if (isDark) Color(0xFFFFB74D).copy(alpha = 0.22f) else Color(0xFFF57C00).copy(alpha = 0.20f)
    val historyColor = if (isDark) Color(0xFFE0E0E0).copy(alpha = 0.18f) else Color(0xFF5D4037).copy(alpha = 0.18f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(baseGradient))
    ) {
        // --- 1. CANVAS ÇİZİMLERİ (Matematik Grafikleri, Pusula, Sütunlar, Atom) ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // --- A. MATEMATİK: Sinüs Dalga Grafiği & Kartezyen Eksen (Sol Üst) ---
            val sinePath = Path()
            val waveAmplitude = 40.dp.toPx()
            val waveFrequency = 0.015f
            val startY = height * 0.12f

            sinePath.moveTo(0f, startY)
            for (x in 0..width.toInt() step 10) {
                val y = startY + sin(x * waveFrequency) * waveAmplitude
                sinePath.lineTo(x.toFloat(), y)
            }
            drawPath(
                path = sinePath,
                color = mathColor,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                )
            )

            // Kartezyen Koordinat Ekseni Çizimi (Sağ Üst)
            val axisCenterX = width * 0.82f
            val axisCenterY = height * 0.16f
            val axisSize = 70.dp.toPx()
            // X Ekseni
            drawLine(
                color = mathColor,
                start = Offset(axisCenterX - axisSize, axisCenterY),
                end = Offset(axisCenterX + axisSize, axisCenterY),
                strokeWidth = 2.dp.toPx()
            )
            // Y Ekseni
            drawLine(
                color = mathColor,
                start = Offset(axisCenterX, axisCenterY - axisSize),
                end = Offset(axisCenterX, axisCenterY + axisSize),
                strokeWidth = 2.dp.toPx()
            )
            // Parabol Eğrisi y = x^2
            val parabolaPath = Path()
            parabolaPath.moveTo(axisCenterX - axisSize * 0.7f, axisCenterY - axisSize * 0.8f)
            parabolaPath.quadraticBezierTo(
                axisCenterX, axisCenterY + axisSize * 0.6f,
                axisCenterX + axisSize * 0.7f, axisCenterY - axisSize * 0.8f
            )
            drawPath(
                path = parabolaPath,
                color = secondaryAccent.copy(alpha = 0.25f),
                style = Stroke(width = 2.dp.toPx())
            )


            // --- B. COĞRAFYA: Pusula Gülü / Rüzgar Kadranı & Paralel-Meridyen Çizgileri (Sağ Üst / Orta) ---
            val compassCenterX = width * 0.85f
            val compassCenterY = height * 0.38f
            val compassRadius = 65.dp.toPx()

            // Dış Pusula Halkası
            drawCircle(
                color = geoColor,
                center = Offset(compassCenterX, compassCenterY),
                radius = compassRadius,
                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
            )
            drawCircle(
                color = geoColor,
                center = Offset(compassCenterX, compassCenterY),
                radius = compassRadius * 0.75f,
                style = Stroke(width = 1.dp.toPx())
            )

            // Pusula İğnesi (Kuzey-Güney)
            val needlePathNorth = Path().apply {
                moveTo(compassCenterX, compassCenterY - compassRadius * 0.9f)
                lineTo(compassCenterX - 8.dp.toPx(), compassCenterY)
                lineTo(compassCenterX + 8.dp.toPx(), compassCenterY)
                close()
            }
            drawPath(path = needlePathNorth, color = secondaryAccent.copy(alpha = 0.30f))

            val needlePathSouth = Path().apply {
                moveTo(compassCenterX, compassCenterY + compassRadius * 0.9f)
                lineTo(compassCenterX - 8.dp.toPx(), compassCenterY)
                lineTo(compassCenterX + 8.dp.toPx(), compassCenterY)
                close()
            }
            drawPath(path = needlePathSouth, color = geoColor)


            // --- C. KİMYA: Benzen Halkası & Atom Yörüngeleri (Sol Orta / Alt) ---
            val benzeneCenterX = width * 0.15f
            val benzeneCenterY = height * 0.45f
            val hexRadius = 45.dp.toPx()

            val hexPath = Path()
            for (i in 0 until 6) {
                val angle = Math.toRadians((i * 60 - 30).toDouble())
                val x = benzeneCenterX + (hexRadius * cos(angle)).toFloat()
                val y = benzeneCenterY + (hexRadius * sin(angle)).toFloat()
                if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
            }
            hexPath.close()

            drawPath(path = hexPath, color = chemColor, style = Stroke(width = 2.dp.toPx()))
            // İç Çember
            drawCircle(
                color = chemColor,
                center = Offset(benzeneCenterX, benzeneCenterY),
                radius = hexRadius * 0.55f,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // Atom Yörüngesi (Elliptical Orbits)
            drawArc(
                color = chemColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(benzeneCenterX - 60.dp.toPx(), benzeneCenterY + 70.dp.toPx()),
                size = Size(120.dp.toPx(), 45.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )


            // --- D. TARİH: Klasik Tarihi Sütunlar & Antik Kemer Silüeti (Alt Kısım) ---
            val pillarWidth = 32.dp.toPx()
            val pillarHeight = 140.dp.toPx()

            // Sol Sütun
            val leftPillarX = width * 0.08f
            val pillarBaseY = height * 0.92f

            // Sütun Gövdesi
            drawRect(
                color = historyColor,
                topLeft = Offset(leftPillarX, pillarBaseY - pillarHeight),
                size = Size(pillarWidth, pillarHeight),
                style = Stroke(width = 1.5.dp.toPx())
            )
            // Sütun Dikey Çizgileri
            for (i in 1..3) {
                val lineX = leftPillarX + (pillarWidth / 4) * i
                drawLine(
                    color = historyColor,
                    start = Offset(lineX, pillarBaseY - pillarHeight + 10.dp.toPx()),
                    end = Offset(lineX, pillarBaseY - 10.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }
            // Sütun Başlığı
            drawRect(
                color = historyColor,
                topLeft = Offset(leftPillarX - 6.dp.toPx(), pillarBaseY - pillarHeight - 12.dp.toPx()),
                size = Size(pillarWidth + 12.dp.toPx(), 12.dp.toPx())
            )

            // Sağ Sütun
            val rightPillarX = width * 0.84f
            drawRect(
                color = historyColor,
                topLeft = Offset(rightPillarX, pillarBaseY - pillarHeight),
                size = Size(pillarWidth, pillarHeight),
                style = Stroke(width = 1.5.dp.toPx())
            )
            for (i in 1..3) {
                val lineX = rightPillarX + (pillarWidth / 4) * i
                drawLine(
                    color = historyColor,
                    start = Offset(lineX, pillarBaseY - pillarHeight + 10.dp.toPx()),
                    end = Offset(lineX, pillarBaseY - 10.dp.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
            }
            drawRect(
                color = historyColor,
                topLeft = Offset(rightPillarX - 6.dp.toPx(), pillarBaseY - pillarHeight - 12.dp.toPx()),
                size = Size(pillarWidth + 12.dp.toPx(), 12.dp.toPx())
            )

            // İki sütun arası Antik Kemer
            val archPath = Path().apply {
                moveTo(leftPillarX + pillarWidth / 2, pillarBaseY - pillarHeight - 12.dp.toPx())
                quadraticBezierTo(
                    width * 0.5f, pillarBaseY - pillarHeight - 70.dp.toPx(),
                    rightPillarX + pillarWidth / 2, pillarBaseY - pillarHeight - 12.dp.toPx()
                )
            }
            drawPath(
                path = archPath,
                color = historyColor,
                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f))
            )
        }

        // --- 2. YAZISAL SEMBOLLER VE PERİYODİK TABLO ELEMENT KARTLARI ---
        // A. Periyodik Tablo Kartı 1: 6 C (Karbon)
        PeriodicElementChip(
            symbol = "C",
            number = "6",
            name = "Karbon",
            color = PrimaryPurple,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 24.dp, top = 48.dp)
                .alpha(0.35f)
        )

        // B. Periyodik Tablo Kartı 2: 12 Mg (Magnezyum)
        PeriodicElementChip(
            symbol = "Mg",
            number = "12",
            name = "Magnezyum",
            color = SecondaryOrange,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp)
                .offset(y = (-80).dp)
                .alpha(0.35f)
        )

        // C. Periyodik Tablo Kartı 3: 79 Au (Altın)
        PeriodicElementChip(
            symbol = "Au",
            number = "79",
            name = "Altın",
            color = Color(0xFFFFC107),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 28.dp, bottom = 120.dp)
                .alpha(0.35f)
        )

        // D. Matematik Formülleri (Yüzen Metinler)
        Text(
            text = "f(x) = ∫ sin(x) dx",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = primaryAccent.copy(alpha = 0.25f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 36.dp, top = 85.dp)
                .rotate(12f)
        )

        Text(
            text = "E = m · c²",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = secondaryAccent.copy(alpha = 0.30f),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 30.dp)
                .offset(y = (-110).dp)
                .rotate(-15f)
        )

        Text(
            text = "π ≈ 3.14159  |  Σ xᵢ",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = primaryAccent.copy(alpha = 0.25f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 40.dp, bottom = 150.dp)
        )

        // Coğrafya Yön Göstergesi
        Text(
            text = "K (N)  •  G (S)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = geoColor.copy(alpha = 0.40f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 40.dp, top = 270.dp)
        )

        // --- 3. ANA İÇERİK KATMANI ---
        content()
    }
}

/**
 * Periyodik Tablo Çip/Kart Tasarımı
 */
@Composable
private fun PeriodicElementChip(
    symbol: String,
    number: String,
    name: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = number,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = symbol,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = name,
                fontSize = 8.sp,
                fontWeight = FontWeight.Normal,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}
