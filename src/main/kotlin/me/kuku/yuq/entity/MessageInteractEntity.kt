package me.kuku.yuq.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "message_interact")
class MessageInteractEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    var telegramMessageId: Int = 0
    @OneToOne
    @JoinColumn(name = "message_id")
    var messageEntity: MessageEntity? = null
}

interface MessageInteractRepository: JpaRepository<MessageInteractEntity, Int>, QuerydslPredicateExecutor<MessageInteractEntity> {
    fun findByTelegramMessageId(telegramMessageId: Int): MessageInteractEntity?
}

@Service
class MessageInteractService (
    private val messageInteractRepository: MessageInteractRepository
) {
    fun save(messageInteractEntity: MessageInteractEntity): MessageInteractEntity =
        messageInteractRepository.save(messageInteractEntity)

    fun findByTelegramMessageId(telegramMessageId: Int) =
        messageInteractRepository.findByTelegramMessageId(telegramMessageId)
}