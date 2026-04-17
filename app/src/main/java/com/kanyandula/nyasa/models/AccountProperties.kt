package com.kanyandula.nyasa.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
@Entity(tableName = "account_properties")
data class AccountProperties(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "pk")
    var pk: Int,

    @ColumnInfo(name = "email")
    var email: String,

    @ColumnInfo(name = "username")
    var username: String,

    @ColumnInfo(name = "bio")
    var bio: String? = null,

    @ColumnInfo(name = "location")
    var location: String? = null,

    @ColumnInfo(name = "website")
    var website: String? = null,

    @ColumnInfo(name = "twitter")
    var twitter: String? = null,

    @ColumnInfo(name = "facebook")
    var facebook: String? = null,

    @ColumnInfo(name = "instagram")
    var instagram: String? = null,

    @ColumnInfo(name = "linkedin")
    var linkedin: String? = null,

    @ColumnInfo(name = "profile_image")
    var profile_image: String? = null
) : Parcelable
