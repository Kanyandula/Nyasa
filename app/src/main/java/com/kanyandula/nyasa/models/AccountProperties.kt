package com.kanyandula.nyasa.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "account_properties")
data class AccountProperties(

    @SerializedName("pk")
    @Expose
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "pk")
    var pk: Int,

    @SerializedName("email")
    @Expose
    @ColumnInfo(name = "email")
    var email: String,

    @SerializedName("username")
    @Expose
    @ColumnInfo(name = "username")
    var username: String,

    @SerializedName("bio")
    @Expose
    @ColumnInfo(name = "bio")
    var bio: String? = null,

    @SerializedName("location")
    @Expose
    @ColumnInfo(name = "location")
    var location: String? = null,

    @SerializedName("website")
    @Expose
    @ColumnInfo(name = "website")
    var website: String? = null,

    @SerializedName("twitter")
    @Expose
    @ColumnInfo(name = "twitter")
    var twitter: String? = null,

    @SerializedName("facebook")
    @Expose
    @ColumnInfo(name = "facebook")
    var facebook: String? = null,

    @SerializedName("instagram")
    @Expose
    @ColumnInfo(name = "instagram")
    var instagram: String? = null,

    @SerializedName("linkedin")
    @Expose
    @ColumnInfo(name = "linkedin")
    var linkedin: String? = null,

    @SerializedName("profile_image")
    @Expose
    @ColumnInfo(name = "profile_image")
    var profile_image: String? = null
) : Parcelable
