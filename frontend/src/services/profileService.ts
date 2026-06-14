import { clearProductState, loadProductState, saveProductState } from '../utils/storage'

export async function getProfileProductData() {
  return loadProductState()
}

export async function clearMyPortraitData() {
  clearProductState()
}

export async function revokeAuthorization(code: string) {
  const state = loadProductState()
  state.invites = state.invites.map((invite) => invite.code === code ? { ...invite, status: 'revoked' } : invite)
  saveProductState(state)
}
