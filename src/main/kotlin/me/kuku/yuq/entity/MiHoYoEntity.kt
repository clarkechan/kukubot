package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "mi_ho_yo")
class MiHoYoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity? = null
    @Column(length = 2000)
    var cookie: String = ""
}

interface MiHoYoRepository: JpaRepository<MiHoYoEntity, Int>

class MiHoYoService @Inject constructor(
    private val miHoYoRepository: MiHoYoRepository
) {
    fun save(miHoYoEntity: MiHoYoEntity): MiHoYoEntity = miHoYoRepository.save(miHoYoEntity)
}