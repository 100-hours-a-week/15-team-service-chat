package com.sipomeokjo.commitme.domain.chat.document;

import com.sipomeokjo.commitme.domain.chat.entity.ChatAttachmentType;
import lombok.Builder;
import java.time.Instant;

@Builder
public record ChatAttachmentEmbedded (
		Long legacyId,
		ChatAttachmentType fileType,
		String fileUrl,
		Integer orderNo,
		Instant createdAt
){}
