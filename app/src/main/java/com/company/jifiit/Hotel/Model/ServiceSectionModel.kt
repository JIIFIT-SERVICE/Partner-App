package com.company.jifiit.Hotel.Model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServiceSectionModel(
    val title: String,
    val subtitle: String,
    val items: List<ServiceItemModel>
) : Parcelable