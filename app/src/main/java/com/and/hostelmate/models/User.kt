package com.and.hostelmate.models

data class User(
    val id: String = "",
    var email: String = "",
    var role: String  = "",
    var name: String  = "",
    var age: Int = 0,
    val cnic: String = "",
    var image: String? = null,
    var phoneNo: String = "",
    var homeAddress: String = "",
    val fatherCnic: String = "",
    val fatherPhoneNo: String = ""
) {
    constructor() : this(
        id = "dummy-id",
        email = "dummy@example.com",
        role = "student",
        name = "Dummy Name",
        age = 20,
        cnic = "12345-6789012-3",
        image = null,
        phoneNo = "0123456789",
        homeAddress = "123 Dummy Street",
        fatherCnic = "98765-4321098-7",
        fatherPhoneNo = "0987654321"
    )
}
