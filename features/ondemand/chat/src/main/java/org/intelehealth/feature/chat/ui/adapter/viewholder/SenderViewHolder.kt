package org.intelehealth.feature.chat.ui.adapter.viewholder

import org.intelehealth.feature.chat.databinding.RowMsgItemSenderBinding
import org.intelehealth.feature.chat.model.ChatMessage


/**
 * Created by Vaghela Mithun R. on 15-08-2023 - 00:18.
 * Email : mithun@intelehealth.org
 * Mob   : +919727206702
 **/
class SenderViewHolder(val binding: RowMsgItemSenderBinding) : BaseViewHolder(binding.root) {

    fun bind(chatMessage: ChatMessage){
        binding.chatMessage = chatMessage
    }
}