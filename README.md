[![GitHub license](https://img.shields.io/github/license/Day8/re-frame-forward-events-fx.svg)](license.txt)
[![Circle CI](https://circleci.com/gh/Day8/re-frame-forward-events-fx/tree/master.svg?style=shield&circle-token=:circle-ci-badge-token)](https://circleci.com/gh/Day8/re-frame-forward-events-fx/tree/master)
[![Circle CI](https://circleci.com/gh/Day8/re-frame-forward-events-fx/tree/develop.svg?style=shield&circle-token=:circle-ci-badge-token)](https://circleci.com/gh/Day8/re-frame-forward-events-fx/tree/develop)
<!--
[![Sample Project](https://img.shields.io/badge/project-example-ff69b4.svg)](https://github.com/Day8/re-frame-forward-events-fx/sample)
-->

# re-frame-forward-events-fx

This re-frame library contains an [Effect Handler](https://github.com/Day8/re-frame/tree/develop/docs),
keyed `:forward-events`, which allows you to listen-for, and then post-process events, typically for higher-level
control flow purposes (eg. coordination).

## Quick Start Guide

### Step 1. Add Dependency

Add the following project dependency:  <br> 
[![Clojars Project](https://img.shields.io/clojars/v/day8.re-frame/forward-events-fx.svg)](https://clojars.org/day8.re-frame/forward-events-fx)

Also requires re-frame >= v0.8.0

### Step 2. Registration And Use

In the namespace where you register your event handlers, perhaps called `events.cljs`, you have 2 things to do.

**First**, add this require to the `ns`:
```clj
(ns app.events
  (:require
    ...
    [day8.re-frame.forward-events-fx]   ;; <-- add this
    ...))
```


Because we never subsequently use this require, it
appears redundant.  But its existence will cause the `:forward-events` effect
handler to self-register with re-frame, which is important
to everything that follows.

**Second**, use it when writing an effectful event handler:
```clj
(reg-event-fx             ;; note the -fx
  :my-event
  (fn [world event]       ;; note: world
    {:db   (assoc (:db world) :some :thing)          ;; optional update to db
     :forward-events  {:register  :coordinator1      ;;  <-- used
                       :events      #{:event1  :event2}
                       :dispatch-to [:coordinator 1]}}))
```

Notice the use of an effect `:forward-event`.  This library defines the "effect handler" which implements `:forward-events`.

## Tutorial

This effect handler provides a way to "forward" events. To put it another way,
it provides a way to listen-for and then post-process events. Some might say it allows you to "sniff" certain events.

Normally, when `(dispatch [:a 42])` happens, the event will be routed to
the registered handler for `:a`, and that's the end of the matter.

But, with this effect, you can specify that a particular set of events be
forwarded to another handler for further processing __after__ normal handling.
This 2nd handler can then further process the events, often carrying out
some sort of meta level, coordination function.

The "forwarding" is done via a 2nd dispatch. The payload of this `dispatch`
is the __entire__ event dispatched in the first place.

`:forward-events` accepts the following keys (all mandatory):
  - `:register` - an id, typically a keyword. Used when you later want to unregister a forwarder. Should be unique across all `:forward-event` effects.
  - `:events` - the set of events for which you'd like to "listen"
  - `:dispatch-to` a vector which represents the template for the "further event" to dispatch.  The
    detected event is provided (conj-ed) to this event template.

For clarity, here's a worked example. If you registered a ":forward-events" for event `:a`  and you gave a `:dispatch-to` of `[:later :blah]`, then:
  - when if any `(dispatch [:a 42])` happened,
  - the handler for `:a` would be run normally. No change so far.
  - but then a further dispatch would be happen:  `(dispatch [:later :blah [:a 42]])`. The entire first event `[:a 42]` is "forwarded" in the further `dispatch`.

Examples of use:
```clj
{:forward-events {:register    :an-id-for-this-listener
                  :events      #{:event1  :event2}
                  :dispatch-to [:later "blah"]}    ;; the forwarded event is conj to the end of this event vec
```

```clj
{:forward-events  {:unregister :the-id-supplied-when-registering}}
```

If you want to unregister watcher that you're not sure if exists, you can pass `:silent-failure?` to prevent throwing an error. 
```clj
{:forward-events  {:unregister :maybe-non-existent-id
                   :silent-failure? true}}
```

When necessary, the value of `:forward-events` can also be a `list` of `maps`,
with each map either registering or unregistering.

