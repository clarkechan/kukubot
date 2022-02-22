package me.kuku.yuq.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.icecreamqaq.yuq.artqq.message.ArtGroupMessageSource
import com.icecreamqaq.yuq.artqq.message.ArtMessageSource
import com.icecreamqaq.yuq.artqq.message.ArtToGroupMessageSource
import com.querydsl.core.BooleanBuilder
import com.vladmihalcea.hibernate.type.json.JsonType
import me.kuku.yuq.utils.plus
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import java.time.LocalDateTime
import javax.inject.Inject
import javax.persistence.*

@Entity
@Table(name = "message")
@TypeDef(name = "json", typeClass = JsonType::class)
class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    var messageId: Int = 0
    @OneToOne
    @JoinColumn(name = "qq_id")
    var qqEntity: QqEntity = QqEntity()
    @OneToOne
    @JoinColumn(name = "group_id")
    var groupEntity: GroupEntity = GroupEntity()
    @Column(length = 5000)
    var content: String = ""
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    var localDateTime: LocalDateTime = LocalDateTime.now()
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var messageSource: MessageSource? = null
}

interface MessageRepository: JpaRepository<MessageEntity, Int>, QuerydslPredicateExecutor<MessageEntity> {
    fun findByMessageIdAndGroupEntity(messageId: Int, groupEntity: GroupEntity): MessageEntity?
    fun findByLocalDateTimeAfter(localDateTime: LocalDateTime): List<MessageEntity>
}

class MessageService @Inject constructor(
    private val messageRepository: MessageRepository
) {

    fun save(messageEntity: MessageEntity): MessageEntity = messageRepository.save(messageEntity)

    fun findByMessageIdAndGroupEntity(messageId: Int, groupEntity: GroupEntity) =
        messageRepository.findByMessageIdAndGroupEntity(messageId, groupEntity)

    fun findByMessageIdAndGroup(messageId: Int, group: Long): MessageEntity? {
        val q = QMessageEntity.messageEntity
        return messageRepository.findOne(q.messageId.eq(messageId).and(q.groupEntity.group.eq(group))).orElse(null)
    }

    fun findByGroupAndQqOrderByIdDesc(group: Long, qq: Long): List<MessageEntity> {
        with(QMessageEntity.messageEntity) {
            return messageRepository.findAll(groupEntity.group.eq(group).and(qqEntity.qq.eq(qq)), id.desc()).toList()
        }
    }

    fun findAll(group: Long?, contentPa: String?, qq: Long?, pageRequest: PageRequest): Page<MessageEntity> {
        with(QMessageEntity.messageEntity) {
            val bb = BooleanBuilder()
            group?.let { bb + groupEntity.group.eq(it) }
            contentPa?.let{ bb + content.like("%$it%") }
            qq?.let { bb + qqEntity.qq.eq(qq) }
            return messageRepository.findAll(bb, pageRequest.withSort(Sort.by("id").descending()))
        }
    }

    fun findByLocalDateTimeAfter(localDateTime: LocalDateTime) = messageRepository.findByLocalDateTimeAfter(localDateTime)

    fun findByGroupAndLocalDateTimeAfter(group: Long, localDateTimeParam: LocalDateTime): List<MessageEntity> {
        with(QMessageEntity.messageEntity) {
            return messageRepository.findAll(groupEntity.group.eq(group) + localDateTime.after(localDateTimeParam)).toList()
        }

    }
}

data class MessageSource(
    val id: Int,
    val qq: Long,
    val groupCode: Long,
    val rand: Int,
    val sendTime: Long,
    val liteMsg: String
) {
    fun toArtGroupMessageSource(): ArtGroupMessageSource {
        return ArtGroupMessageSource(id, qq, groupCode, rand, sendTime, liteMsg)
    }
}


fun ArtMessageSource.toMessageSource(): MessageSource? {
    return when (this) {
        is ArtGroupMessageSource -> MessageSource(this.id, this.qq, this. groupCode, this.rand, this.sendTime, this.liteMsg)
        is ArtToGroupMessageSource -> MessageSource(this.id, this.qq, this. groupCode, this.rand, this.sendTime, this.liteMsg)
        else -> null
    }
}
