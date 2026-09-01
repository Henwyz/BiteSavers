package com.example.bitesavers.customer.details.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bitesavers.R
import com.example.bitesavers.ui.theme.BiteSaversTheme

@Composable
fun FoodDetailHero(
    imageUrl: String?,
    imageResId: Int = R.drawable.food_spaghetti,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            // Loads remote network image from Supabase with a local drawable placeholder
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .error(imageResId)
                    .placeholder(imageResId)
                    .build(),
                contentDescription = stringResource(id = R.string.cd_food_item_image),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback to local drawable if no network image URL is present
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = stringResource(id = R.string.cd_food_item_image),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Preview(showBackground = true, name = "Food Detail Hero - Local Drawable")
@Composable
private fun FoodDetailHeroPreview() {
    BiteSaversTheme {
        FoodDetailHero(
            imageUrl = null,
            imageResId = R.drawable.food_spaghetti
        )
    }
}