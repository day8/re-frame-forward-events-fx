<!-- [![CI](https://github.com/day8/re-frame-forward-events-fx/workflows/ci/badge.svg)](https://github.com/day8/re-frame-forward-events-fx/actions?workflow=ci)
[![CD](https://github.com/day8/re-frame-forward-events-fx/workflows/cd/badge.svg)](https://github.com/day8/re-frame-forward-events-fx/actions?workflow=cd)
[![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/day8/re-frame-forward-events-fx?style=flat)](https://github.com/day8/re-frame-forward-events-fx/tags) -->
[![Clojars Project](https://img.shields.io/clojars/v/day8.re-frame/forward-events-fx?style=for-the-badge&logo=clojure&logoColor=fff)](https://clojars.org/day8.re-frame/forward-events-fx)
[![GitHub issues](https://img.shields.io/github/issues-raw/day8/re-frame-forward-events-fx?style=for-the-badge&logo=github)](https://github.com/day8/re-frame-forward-events-fx/issues)
<!-- [![GitHub pull requests](https://img.shields.io/github/issues-pr/day8/re-frame-forward-events-fx?style=for-the-badge&logo=github)](https://github.com/day8/re-frame-forward-events-fx/pulls) -->
[![License](https://img.shields.io/github/license/day8/re-frame-forward-events-fx?style=for-the-badge)](license.txt)
<!--
[![Sample Project](https://img.shields.io/badge/project-example-ff69b4.svg)](https://github.com/day8/re-frame-forward-events-fx/sample)
-->

# re-frame-forward-events-fx

This re-frame library contains an [Effect Handler](https://github.com/day8/re-frame/tree/develop/docs),
keyed `:forward-events`, which allows you to listen-for, and then post-process events, typically for higher-level
control flow purposes (eg. coordination).

## Quick Start Guide

### Step 1. Add Dependency

Add the following project dependency:  <br> 
[![clojars](https://img.shields.io/clojars/v/day8.re-frame/forward-events-fx?style=for-the-badge&logo=clojure&logoColor=fff)](https://clojars.org/day8.re-frame/forward-events-fx)

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

When necessary, the value of `:forward-events` can also be a `list` of `maps`,
with each map either registering or unregistering.

## Testing

To run the tests in a browser

```
lein dev
```

To run the tests with Karma (i.e. for continuous integration)

```
sudo npm -g install karma-cli
lein ci
```
