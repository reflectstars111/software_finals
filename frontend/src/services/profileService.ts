import { testApi, recommendationApi, matchApi, reportApi } from '../api'

export async function getProfileData() {
  const [testHistory, reportSnapshots, matches, shares, feedbacks, invites] = await Promise.all([
    testApi.history(),
    reportApi.history(),
    matchApi.list(),
    reportApi.shares(),
    recommendationApi.myFeedback(),
    matchApi.listInvites()
  ])
  return { testHistory, reportSnapshots, matches, shares, feedbacks, invites }
}
