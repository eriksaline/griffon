package greet

import java.beans.PropertyChangeListener

class UserPaneController {
    // these will be injected by Griffon
    UserPaneModel model
    UserPaneView view

    TimelinePaneController timelinePaneController

    TwitterService twitterService

    void mvcGroupInit(Map args) {
        twitterService = app.controllers.Greet.twitterService
        def timelinePaneModel, timelinePaneView
        (timelinePaneModel, timelinePaneView, timelinePaneController) =
            createMVCGroup('TimelinePane', "TimelinePane_user_$args.user.screen_name", [:]);
        timelinePaneController.twitterService = twitterService
        timelinePaneModel.tweetListGenerator = {TwitterService twitterService ->
            def thisScreenName = model.screenName
            def tweets = twitterService.tweetCache.values().findAll {
                ((model.showTweets && (it.user.screen_name == thisScreenName))
                || (model.showReplies && (it.in_reply_to_screen_name == thisScreenName)))
            }
            tweets.addAll (twitterService.dmCache.values().findAll {
                model.showDirectMessages && ((it.sender_screen_name == thisScreenName)
                || (it.recipient_screen_name == thisScreenName))
            })
            tweets.sort( {a, b-> b.created_at <=> a.created_at}).collect { it.id }
        }

        view.closable = args.closable
        model.user = args.user
        model.screenName = model.user.screen_name
        model.following = twitterService.currentUserFollows(args.user.id)
        model.follows = twitterService.follows(args.user.id, twitterService.authenticatedUser.id)

        // fixme add after init?
        model.addPropertyChangeListener("showTweets", this.&updateTimeline as PropertyChangeListener)
        model.addPropertyChangeListener("showReplies", this.&updateTimeline as PropertyChangeListener)
        model.addPropertyChangeListener("showDirectMessages", this.&updateTimeline as PropertyChangeListener)
        view.timelinePane = timelinePaneView.timeline
    }

    def updateTimeline(evt) {
        timelinePaneController.updateTimeline(evt)
    }

    def close(evt) {
        view.userPane.parent.remove(view.userPane)
        destroyMVCGroup("UserPane_$model.user.screen_name")
        destroyMVCGroup("TimelinePane_user_$model.user.screen_name")
    }

    def follow(evt) {
        model.busy = true
        String userID = model.user.id
        doOutside {
            try {
                twitterService.follow(userID)
            } finally {
                edt {
                    model.following = twitterService.currentUserFollows(userID)
                    model.busy = false
                }
            }
        }
    }

    def unfollow(evt) {
      model.busy = true
      String userID = model.user.id
      doOutside {
          try {
              twitterService.unfollow(userID)
          } finally {
              edt {
                  model.following = twitterService.currentUserFollows(userID)
                  model.busy = false
              }
          }
      }
    }

    def directMessage(evt) {
        app.models.Greet.sendingDM = true
        app.models.Greet.targetUser = model.user.screen_name
    }
}