package com.and.hostelmate.models

data class UserRequest(
    var id: Int = 0,
    var name: String = "",
    var pass: String = "",
    var email: String = "",
    var cnic: String = "",
    var age: String = "",
    var preferRoom: Int = 0,
    var phone: String = "",
    var address: String = "",
    var fatherCNIC: String = "",
    var fatherPhoneNo: String = "",
    var image: String?,
)
