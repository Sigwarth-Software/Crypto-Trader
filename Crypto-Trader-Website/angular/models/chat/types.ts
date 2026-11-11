export type ChatMessage = {
    role: 'user' | 'assistant'
    content: string
    timestamp: Date
}

export type ChatConversation = {
    id: string
    title: string
    messages: ChatMessage[]
    updatedAt: Date
}

export type InitConversationResponse = {
    conversationId: string
}

export interface ChatMessageRequest {
    messageContent: string
}

export interface ConversationResponse {
    conversationId: string
    title: string
}
export interface ConversationMessageResponse {
    messageContent: string
    author: MessageAuthorSource
}
export enum MessageAuthorSource {
    User = 'USER',
    Bot = 'BOT'
}