package cloud.carvis.api.integration

import cloud.carvis.api.AbstractApplicationTest
import cloud.carvis.api.AbstractApplicationTest.Users.VALID_USER_ID
import cloud.carvis.api.AbstractApplicationTest.Users.VALID_USER_NAME
import cloud.carvis.api.cars.dao.CarRepository
import cloud.carvis.api.cars.model.CarDto
import cloud.carvis.api.common.properties.S3Buckets
import cloud.carvis.api.users.model.UserDto
import cloud.carvis.api.util.testdata.TestData
import com.amazonaws.services.s3.AmazonS3
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.hasItem
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant.now
import java.util.*


class CarRestControllerTest : AbstractApplicationTest() {

    @Autowired
    private lateinit var carRepository: CarRepository

    @Autowired
    private lateinit var s3Properties: S3Buckets

    @Autowired
    private lateinit var amazonS3: AmazonS3

    @Test
    @WithMockUser
    fun `cars GET - no cars`() {
        this.mockMvc.perform(get("/cars"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()", equalTo(0)))
    }

    @Test
    @WithMockUser(username = VALID_USER_ID)
    fun `cars GET - one cars`() {
        // given
        auth0Mock.withUsers(UserDto(userId = VALID_USER_ID, name = VALID_USER_NAME))
        val car = testDataGenerator
            .withCar(VALID_USER_ID)
            .getCar()
            .value()

        // when / then
        this.mockMvc.perform(get("/cars"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()", equalTo(1)))
            .andExpect(jsonPath("$.[0].id").value(car.id.toString()))
            .andExpect(jsonPath("$.[0].brand").value(car.brand))
            .andExpect(jsonPath("$.[0].bodyType").value(car.bodyType))
            .andExpect(jsonPath("$.[0].ads").value(car.ads))
            .andExpect(jsonPath("$.[0].additionalEquipment").value(car.additionalEquipment))
            .andExpect(jsonPath("$.[0].capacity").value(car.capacity))
            .andExpect(jsonPath("$.[0].colorAndMaterialInterior").value(car.colorAndMaterialInterior))
            .andExpect(jsonPath("$.[0].colorExterior").value(car.colorExterior))
            .andExpect(jsonPath("$.[0].colorExteriorManufacturer").value(car.colorExteriorManufacturer))
            .andExpect(jsonPath("$.[0].condition").value(car.condition))
            .andExpect(jsonPath("$.[0].countryOfOrigin").value(car.countryOfOrigin))
            .andExpect(jsonPath("$.[0].createdAt").value(car.createdAt.toString()))
            .andExpect(jsonPath("$.[0].createdBy").value(VALID_USER_ID))
            .andExpect(jsonPath("$.[0].description").value(car.description))
            .andExpect(jsonPath("$.[0].horsePower").value(car.horsePower))
            .andExpect(jsonPath("$.[0].images").value(car.images.map { it.toString() }))
            .andExpect(jsonPath("$.[0].mileage").value(car.mileage))
            .andExpect(jsonPath("$.[0].modelDetails").value(car.modelDetails))
            .andExpect(jsonPath("$.[0].modelSeries").value(car.modelSeries))
            .andExpect(jsonPath("$.[0].modelYear").value(car.modelYear))
            .andExpect(jsonPath("$.[0].ownerName").value(VALID_USER_NAME))
            .andExpect(jsonPath("$.[0].price").value(car.price))
            .andExpect(jsonPath("$.[0].shortDescription").value(car.shortDescription))
            .andExpect(jsonPath("$.[0].transmission").value(car.transmission))
            .andExpect(jsonPath("$.[0].type").value(car.type))
            .andExpect(jsonPath("$.[0].updatedAt").value(car.updatedAt.toString()))
            .andExpect(jsonPath("$.[0].vin").value(car.vin))
    }

    @Test
    @WithMockUser
    fun `car GET - not found`() {
        this.mockMvc.perform(get("/cars/{id}", UUID.randomUUID()))
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockUser
    fun `car GET - found`() {
        // given
        val car = testDataGenerator
            .withCar()
            .getCar()
            .value()

        // when / then
        this.mockMvc.perform(get("/cars/{id}", car.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(car.id.toString()))
    }

    @Test
    @WithMockUser(username = VALID_USER_ID)
    fun `cars POST - create car success`() {
        // given
        auth0Mock.withUsers(UserDto(userId = VALID_USER_ID, name = VALID_USER_NAME))
        val car: TestData<CarDto> = testDataGenerator.random()
        val start = now()


        // when
        val returnedCar = this.mockMvc
            .perform(
                post("/cars")
                    .content(car.toJson())
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", APPLICATION_JSON.toString()))
            .andReturn()
            .toObject<CarDto>()

        // then
        assertThat(carRepository.count()).isEqualTo(1)
        assertThat(returnedCar.id).isNotNull
        assertThat(returnedCar.ownerName).isEqualTo(VALID_USER_NAME)
        assertThat(returnedCar.createdBy).isEqualTo(VALID_USER_ID)
        assertThat(returnedCar.createdAt).isBetween(start, now())
        assertThat(returnedCar.updatedAt).isEqualTo(returnedCar.createdAt)
        assertThat(returnedCar.updatedBy).isEqualTo(returnedCar.createdBy)
    }

    @Test
    @WithMockUser
    fun `cars POST - body validation`() {
        // 200
        assert(status().isOk, "mileage", 0L)

        // 400
        assert(status().isBadRequest, "brand")
        assert(status().isBadRequest, "bodyType")
        assert(status().isBadRequest, "capacity", -100L)
        assert(status().isBadRequest, "colorAndMaterialInterior")
        assert(status().isBadRequest, "colorExterior")
        assert(status().isBadRequest, "colorExteriorManufacturer")
        assert(status().isBadRequest, "horsePower", -100L)
        assert(status().isBadRequest, "mileage", -100L)
        assert(status().isBadRequest, "modelDetails")
        assert(status().isBadRequest, "modelSeries")
        assert(status().isBadRequest, "modelYear")
        assert(status().isBadRequest, "shortDescription")
        assert(status().isBadRequest, "transmission")
        assert(status().isBadRequest, "type")
    }

    @Test
    @WithMockUser
    fun `cars POST - no payload yields bad requests`() {
        this.mockMvc
            .perform(
                post("/cars")
            )
            .andExpect(status().isBadRequest)
    }

    @Test
    @WithMockUser(username = VALID_USER_ID)
    fun `car PUT - update existing car`() {
        // given
        auth0Mock.withUsers(UserDto(userId = VALID_USER_ID, name = VALID_USER_NAME))
        val start = now()
        val existingCar = testDataGenerator
            .withCar(VALID_USER_ID)
            .getCar()
            .value()
        val carId = existingCar.id.toString()
        val updatedCar: TestData<CarDto> = testDataGenerator.random()

        // when
        val returnedCar = this.mockMvc
            .perform(
                put("/cars/{id}", carId)
                    .content(updatedCar.toJson())
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(carId))
            .andExpect(jsonPath("$.brand").value(updatedCar.value().brand))
            .andExpect(jsonPath("$.bodyType").value(updatedCar.value().bodyType))
            .andExpect(jsonPath("$.ads").value(updatedCar.value().ads))
            .andExpect(jsonPath("$.additionalEquipment").value(updatedCar.value().additionalEquipment))
            .andExpect(jsonPath("$.capacity").value(updatedCar.value().capacity))
            .andExpect(jsonPath("$.colorAndMaterialInterior").value(updatedCar.value().colorAndMaterialInterior))
            .andExpect(jsonPath("$.colorExterior").value(updatedCar.value().colorExterior))
            .andExpect(jsonPath("$.colorExteriorManufacturer").value(updatedCar.value().colorExteriorManufacturer))
            .andExpect(jsonPath("$.condition").value(updatedCar.value().condition))
            .andExpect(jsonPath("$.countryOfOrigin").value(updatedCar.value().countryOfOrigin))
            .andExpect(jsonPath("$.createdAt").value(existingCar.createdAt.toString()))
            .andExpect(jsonPath("$.createdBy").value(VALID_USER_ID))
            .andExpect(jsonPath("$.description").value(updatedCar.value().description))
            .andExpect(jsonPath("$.horsePower").value(updatedCar.value().horsePower))
            .andExpect(jsonPath("$.images").value(updatedCar.value().images.map { it.toString() }))
            .andExpect(jsonPath("$.mileage").value(updatedCar.value().mileage))
            .andExpect(jsonPath("$.modelDetails").value(updatedCar.value().modelDetails))
            .andExpect(jsonPath("$.modelSeries").value(updatedCar.value().modelSeries))
            .andExpect(jsonPath("$.modelYear").value(updatedCar.value().modelYear))
            .andExpect(jsonPath("$.ownerName").value(VALID_USER_NAME))
            .andExpect(jsonPath("$.price").value(updatedCar.value().price))
            .andExpect(jsonPath("$.transmission").value(updatedCar.value().transmission))
            .andExpect(jsonPath("$.type").value(updatedCar.value().type))
            .andExpect(jsonPath("$.updatedBy").value(VALID_USER_ID))
            .andExpect(jsonPath("$.vin").value(updatedCar.value().vin))
            .andReturn()
            .toObject<CarDto>()

        // then
        assertThat(returnedCar.updatedAt).isBetween(start, now())
    }

    @Test
    @WithMockUser(username = VALID_USER_ID)
    fun `car PUT - update other users car forbidden`() {
        // given
        val car = testDataGenerator
            .withCar("bar")
            .getCar()
            .value()

        // when / then
        this.mockMvc
            .perform(
                put("/cars/{id}", car.id)
                    .content(testDataGenerator.random<CarDto>().toJson())
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "foo", roles = ["ADMIN"])
    fun `car PUT - admin can update other users car`() {
        // given
        val start = now()
        val car = testDataGenerator
            .withCar("bar")
            .getCar()
            .value()

        // when / then
        this.mockMvc
            .perform(
                put("/cars/{id}", car.id)
                    .content(testDataGenerator.random<CarDto>().toJson())
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.createdBy").value("bar"))

        // then
        val updatedCar = carRepository.findByHashKey(car.id!!)!!
        assertThat(updatedCar.createdBy).isEqualTo("bar")
        assertThat(updatedCar.createdAt).isEqualTo(car.createdAt)
        assertThat(updatedCar.updatedBy).isEqualTo("foo")
        assertThat(updatedCar.updatedAt).isBetween(start, now())
    }

    @Test
    @WithMockUser
    fun `car PUT - car does not exist`() {
        // given
        val car = testDataGenerator.random<CarDto>()

        // when / then
        this.mockMvc
            .perform(
                put("/cars/{id}", UUID.randomUUID())
                    .content(car.toJson())
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "patrick", roles = ["USER"])
    fun `car PUT - delete images in s3`() {
        // given
        val imagesBucket = s3Properties.images
        val imageId1 = UUID.randomUUID()
        val imageId2 = UUID.randomUUID()
        val car = testDataGenerator
            .withImage(imageId = imageId1)
            .withImage(imageId = imageId2)
            .withCar(createdBy = "patrick", imageIds = listOf(imageId1, imageId2))
            .getCar()
        car.value().images = listOf(imageId1)

        this.mockMvc
            .perform(
                put("/cars/{id}", car.value().id)
                    .content(car.toJson())
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.images.length()").value(1))
            .andExpect(jsonPath("$.images", hasItem(imageId1.toString())))

        awaitAssert {
            assertThat(amazonS3.listObjects(imagesBucket).objectSummaries.count()).isEqualTo(2)
            assertThat(amazonS3.doesObjectExist(imagesBucket, "$imageId1/ORIGINAL")).isTrue
            assertThat(amazonS3.doesObjectExist(imagesBucket, "deleted/$imageId2/ORIGINAL")).isTrue
        }
    }

    @Test
    @WithMockUser(username = VALID_USER_ID)
    fun `cars DELETE - delete car success`() {
        // given
        val car = testDataGenerator
            .withCar(VALID_USER_ID)
            .getCar()
            .value()

        // when
        this.mockMvc
            .perform(delete("/cars/{id}", car.id))
            .andExpect(status().isNoContent)

        // then
        this.mockMvc
            .perform(get("/cars", car.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    @WithMockUser
    fun `car DELETE - car does not exist`() {
        this.mockMvc
            .perform(delete("/cars/{id}", UUID.randomUUID().toString()))
            .andExpect(status().isForbidden)
    }

    @Test
    @WithMockUser(username = "bar", roles = ["ADMIN"])
    fun `car DELETE - admin can delete other users car`() {
        // given
        val car = testDataGenerator
            .withCar("foo")
            .getCar().value()

        // when
        this.mockMvc
            .perform(delete("/cars/{id}", car.id))
            .andExpect(status().isNoContent)

        // then
        this.mockMvc
            .perform(get("/cars", car.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    @WithMockUser(username = "patrick", roles = ["USER"])
    fun `car DELETE - delete images in s3`() {
        // given
        val imagesBucket = s3Properties.images
        val imageId1 = UUID.randomUUID()
        val imageId2 = UUID.randomUUID()
        val car = testDataGenerator
            .withImage(imageId = imageId1)
            .withImage(imageId = imageId2)
            .withCar(createdBy = "patrick", imageIds = listOf(imageId1, imageId2))
            .getCar()

        this.mockMvc
            .perform(
                delete("/cars/{id}", car.value().id)
            )
            .andExpect(status().isNoContent)

        assertThat(carRepository.existsByHashKey(car.value().id!!)).isFalse
        awaitAssert {
            assertThat(amazonS3.listObjects(imagesBucket).objectSummaries.count()).isEqualTo(2)
            assertThat(amazonS3.doesObjectExist(imagesBucket, "deleted/$imageId1/ORIGINAL")).isTrue
            assertThat(amazonS3.doesObjectExist(imagesBucket, "deleted/$imageId2/ORIGINAL")).isTrue
        }
    }

    @Test
    @WithMockUser(username = "patrick", roles = ["USER"])
    fun `car POST - add carId to uploaded images`() {
        // given
        val imagesBucket = s3Properties.images
        val imageId1 = UUID.randomUUID()
        val imageId2 = UUID.randomUUID()
        val car = testDataGenerator
            .withImage(imageId = imageId1)
            .withImage(imageId = imageId2)
            .withCar(createdBy = "patrick", imageIds = listOf(imageId1, imageId2))
            .getCar()

        val createdCar = this.mockMvc
            .perform(
                post("/cars")
                    .content(car.toJson())
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andReturn()
            .toObject<CarDto>()

        awaitAssert {
            assertThat(amazonS3.getObjectMetadata(imagesBucket, "$imageId1/ORIGINAL"))
                .extracting { it.userMetadata["carId"] }.isEqualTo(createdCar.id.toString())
            assertThat(amazonS3.getObjectMetadata(imagesBucket, "$imageId2/ORIGINAL"))
                .extracting { it.userMetadata["carId"] }.isEqualTo(createdCar.id.toString())
        }
    }

    @Test
    @WithMockUser(username = "patrick", roles = ["USER"])
    fun `car PUT - add carId to new images`() {
        // given
        val imagesBucket = s3Properties.images
        val existingImage = UUID.randomUUID()
        val newImage = UUID.randomUUID()
        val car = testDataGenerator
            .withImage(imageId = existingImage)
            .withImage(imageId = newImage)
            .withCar(createdBy = "patrick", imageIds = listOf(existingImage))
            .getCar()
        car.value().images = listOf(existingImage, newImage)

        val createdCar = this.mockMvc
            .perform(
                put("/cars/{carId}", car.value().id)
                    .content(car.toJson())
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isOk)
            .andReturn()
            .toObject<CarDto>()

        awaitAssert {
            assertThat(amazonS3.getObjectMetadata(imagesBucket, "$newImage/ORIGINAL"))
                .extracting { it.userMetadata["carId"] }.isEqualTo(createdCar.id.toString())
        }
    }

    fun assert(httpStatus: ResultMatcher, attribute: String, value: Any? = null) {
        val car: TestData<CarDto> = testDataGenerator.random()
        car.setValue(attribute, value)

        this.mockMvc
            .perform(
                post("/cars")
                    .content(car.toJson())
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(httpStatus)
    }
}
