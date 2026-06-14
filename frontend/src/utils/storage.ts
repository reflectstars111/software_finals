import type { ProductState } from '../productTypes'

const KEY = 'radar_product_state'

export function defaultProductState(): ProductState {
  return {
    answers: {},
    portrait: null,
    testHistory: [],
    feedbacks: [],
    invites: [],
    matchResults: []
  }
}

export function loadProductState(): ProductState {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return defaultProductState()
    return { ...defaultProductState(), ...JSON.parse(raw) } as ProductState
  } catch {
    return defaultProductState()
  }
}

export function saveProductState(state: ProductState) {
  localStorage.setItem(KEY, JSON.stringify(state))
}

export function updateProductState(updater: (state: ProductState) => ProductState): ProductState {
  const next = updater(loadProductState())
  saveProductState(next)
  return next
}

export function clearProductState() {
  localStorage.removeItem(KEY)
}
