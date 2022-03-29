package com.trendyol.kediatr

import java.util.UUID

data class CommandMetadata(val correlationId: UUID, val causationId: UUID)