package com.surya.productcatalog.ui.productdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.surya.productcatalog.R
import com.surya.productcatalog.data.model.Product
import com.surya.productcatalog.ui.components.ErrorState
import com.surya.productcatalog.ui.components.LoadingState
import com.surya.productcatalog.ui.components.formatPrice
import com.surya.productcatalog.ui.components.formatRating
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Int,
    onBack: () -> Unit,
    viewModel: ProductDetailViewModel = viewModel(
        key = "product_$productId",
        factory = ProductDetailViewModel.factory(productId),
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val addedMessage = stringResource(R.string.added_to_cart)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Only offer "Add to cart" once the product has loaded.
            if (uiState is ProductDetailUiState.Success) {
                AddToCartBar(
                    onAddToCart = {
                        viewModel.addToCart()
                        scope.launch {
                            // Replace any snackbar still showing instead of queueing them.
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar(addedMessage)
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (val state = uiState) {
            ProductDetailUiState.Loading -> LoadingState(contentModifier)
            is ProductDetailUiState.Error -> ErrorState(
                message = state.message,
                onRetry = viewModel::loadProduct,
                modifier = contentModifier,
            )
            is ProductDetailUiState.Success -> ProductDetailContent(
                product = state.product,
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun ProductDetailContent(product: Product, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        AsyncImage(
            // The first full-size image looks better than the small thumbnail.
            model = product.images.firstOrNull() ?: product.thumbnail,
            contentDescription = product.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = product.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = formatPrice(product.price),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = formatRating(product.rating),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            InfoRow(
                label = stringResource(R.string.label_category),
                value = product.category.replaceFirstChar { it.uppercase() },
            )
            InfoRow(
                label = stringResource(R.string.label_brand),
                // Brand is missing for some products in the API.
                value = product.brand ?: stringResource(R.string.brand_unknown),
            )
            InfoRow(
                label = stringResource(R.string.label_stock),
                value = if (product.stock > 0) {
                    stringResource(R.string.stock_available, product.stock)
                } else {
                    stringResource(R.string.out_of_stock)
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(text = product.description, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun AddToCartBar(onAddToCart: () -> Unit) {
    Surface(shadowElevation = 8.dp) {
        Button(
            onClick = onAddToCart,
            modifier = Modifier
                // Edge-to-edge draws behind the system navigation bar (back/home buttons);
                // this pushes the button above it while the Surface background still fills it.
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(stringResource(R.string.add_to_cart))
        }
    }
}
