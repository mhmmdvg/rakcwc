import com.rakcwc.domain.models.Products

data class DetailProductState(
    val product: Products? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val quantity: Int = 1,
    val isFavorite: Boolean = false
)