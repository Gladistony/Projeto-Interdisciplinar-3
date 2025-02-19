/*package com.teste.projeto_3.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.teste.projeto_3.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User?)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: String?): User?

    @get:Query("SELECT * FROM users")
    val allUsers: List<User?>?
}*/