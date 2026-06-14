import { demoInviteCodes } from '../data/mockUsers'
import { createMatchResult } from '../utils/matching'
import { loadProductState, saveProductState } from '../utils/storage'

export async function generateMyInvite(userId: string) {
  const state = loadProductState()
  if (!state.portrait) throw new Error('请先完成测试，再生成匹配邀请码。')
  const existing = state.invites.find((invite) => invite.status === 'active')
  if (existing) return existing
  const invite = {
    code: `ME${Math.random().toString(36).slice(2, 8).toUpperCase()}`,
    ownerUserId: userId,
    createdAt: new Date().toISOString(),
    status: 'active' as const
  }
  state.invites = [invite, ...state.invites]
  saveProductState(state)
  return invite
}

export async function matchWithInvite(code: string, userName: string) {
  const state = loadProductState()
  if (!state.portrait) throw new Error('双方数据不足：请先完成自己的测试。')
  if (!code.trim()) throw new Error('请输入对方邀请码。')
  if (!demoInviteCodes.includes(code.trim().toUpperCase())) throw new Error('邀请码错误，请确认后重新输入。')
  const result = createMatchResult(state.portrait, undefined, userName, '示例好友')
  state.matchResults = [result, ...state.matchResults].slice(0, 20)
  saveProductState(state)
  return result
}

export async function revokeInvite(code: string) {
  const state = loadProductState()
  state.invites = state.invites.map((invite) => invite.code === code ? { ...invite, status: 'revoked' } : invite)
  saveProductState(state)
}
