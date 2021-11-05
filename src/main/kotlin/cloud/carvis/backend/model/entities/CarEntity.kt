package cloud.carvis.backend.model.entities

import cloud.carvis.backend.dao.converters.DynamoDbInstantConverter
import com.amazonaws.services.dynamodbv2.datamodeling.*
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@DynamoDBTable(tableName = "carvis-cars")
data class CarEntity(
    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    override var id: UUID? = null,

    @DynamoDBAttribute
    var brand: String? = null,

    @DynamoDBAttribute
    var bodyType: String? = null,

    @DynamoDBAttribute
    var ads: List<String> = emptyList(),

    @DynamoDBAttribute
    var additionalEquipment: String? = null,

    @DynamoDBAttribute
    var capacity: Long? = null,

    @DynamoDBAttribute
    var colorAndMaterialInterior: String? = null,

    @DynamoDBAttribute
    var colorExterior: String? = null,

    @DynamoDBAttribute
    var colorExteriorManufacturer: String? = null,

    @DynamoDBAttribute
    var condition: String? = null,

    @DynamoDBAttribute
    var countryOfOrigin: String? = null,

    @DynamoDBAttribute
    var description: String? = null,

    @DynamoDBAttribute
    var horsePower: Long? = null,

    @DynamoDBAttribute
    var images: List<UUID> = emptyList(),

    @DynamoDBAttribute
    var mileage: Long? = null,

    @DynamoDBAttribute
    var modelDetails: String? = null,

    @DynamoDBAttribute
    var modelSeries: String? = null,

    @DynamoDBAttribute
    var modelYear: String? = null,

    @DynamoDBAttribute
    var price: BigDecimal? = null,

    @DynamoDBAttribute
    var shortDescription: String? = null,

    @DynamoDBAttribute
    var transmission: String? = null,

    @DynamoDBAttribute
    var type: String? = null,

    @DynamoDBAttribute
    var vin: String? = null,

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = DynamoDbInstantConverter::class)
    @CreatedDate
    override var createdAt: Instant? = null,

    @DynamoDBAttribute
    @CreatedBy
    override var ownerUsername: String? = null,

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = DynamoDbInstantConverter::class)
    @LastModifiedDate
    override var updatedAt: Instant? = null,

    @DynamoDBAttribute
    @LastModifiedBy
    override var lastModifiedBy: String? = null
) : Entity()
