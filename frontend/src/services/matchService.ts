import { matchApi } from '../api'

export async function createMatchByPhone(friendPhone: string) {
  return matchApi.create(friendPhone)
}

export async function createMatchByInvite(inviteCode: string) {
  return matchApi.matchByInvite(inviteCode)
}

export async function generateInvite() {
  return matchApi.createInvite()
}

export async function listInvites() {
  return matchApi.listInvites()
}

export async function listMatches() {
  return matchApi.list()
}

export async function getMatch(id: number) {
  return matchApi.get(id)
}
