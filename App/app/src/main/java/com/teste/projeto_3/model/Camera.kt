package com.teste.projeto_3.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Camera(
    var id: Int = -1,
    var nome: String = "",
    var descricao: String = "",
    var codigo_camera: String = ""
) : Parcelable {
    override fun toString(): String {
        return "Camera(id='$id', nome='$nome', descricao='$descricao', codigo_camera='$codigo_camera')"
    }
}