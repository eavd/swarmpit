(ns material.component.list.basic
  (:refer-clojure :exclude [list])
  (:require [material.component :as cmp]
            [swarmpit.url :refer [dispatch!]]
            [sablono.core :refer-macros [html]]
            [rum.core :as rum]))

(defn table-head
  [render-metadata]
  (cmp/table-head
    {:key "Swarmpit-table-head"}
    (cmp/table-row
      {:key "Swarmpit-table-head-row"}
      (map-indexed
        (fn [index header]
          (cmp/table-cell
            {:key       (str "Swarmpit-table-head-cell-" index)
             :className "Swarmpit-table-head-cell"}
            (:name header))) (:summary render-metadata)))))

(defn table-body
  [render-metadata items onclick-handler-fn]
  (cmp/table-body
    {:key "Swarmpit-table-body"}
    (map-indexed
      (fn [index item]
        (cmp/table-row
          {:key     (str "Swarmpit-table-row-" index)
           :onClick #(onclick-handler-fn item)
           :hover   true}
          (->> (:summary render-metadata)
               (map-indexed
                 (fn [coll-index coll]
                   (let [render-fn (:render-fn coll)]
                     (cmp/table-cell
                       {:key       (str "Swarmpit-table-row-cell-" index "-" coll-index)
                        :className "Swarmpit-table-row-cell"}
                       (render-fn item)))))))) items)))

(defn table-raw
  [render-metadata items onclick-handler-fn]
  (cmp/table
    {:key       "Swarmpit-table"
     :className "Swarmpit-table"}
    (table-head render-metadata)
    (table-body render-metadata items onclick-handler-fn)))

(defn table
  [render-metadata items onclick-handler-fn]
  (cmp/card
    {:className "Swarmpit-card"}
    (cmp/card-header
      {:className "Swarmpit-table-card-header"
       :title     (:title render-metadata)
       :subheader (:subheader render-metadata)})
    (cmp/card-content
      {:className "Swarmpit-table-card-content"}
      (table-raw render-metadata items onclick-handler-fn))))

(defn list-item
  [render-metadata index item onclick-handler-fn]
  (let [status-fn (:status-fn render-metadata)
        primary-key (:primary-key render-metadata)
        secodary-key (:secondary-key render-metadata)]
    (cmp/list-item
      {:key     (str "Swarmpit-list-item-" index)
       :button  true
       :onClick #(onclick-handler-fn item)}
      (cmp/list-item-text
        (merge
          {:key     (str "Swarmpit-list-item-text-" index)
           :classes {:primary   "Swarmpit-list-item-text-primary"
                     :secondary "Swarmpit-list-item-text-secondary"}
           :primary (primary-key item)}
          (when secodary-key
            {:secondary (secodary-key item)})))
      (when status-fn
        (cmp/list-item-secondary-action
          {:key   (str "Swarmpit-list-status-" index)
           :style {:marginRight "10px"}}
          (status-fn item))))))

(defn list-raw
  [render-metadata items onclick-handler-fn]
  (cmp/list
    {:dense true}
    (map-indexed
      (fn [index item]
        (list-item
          render-metadata
          index
          item
          onclick-handler-fn)) items)))

(defn list
  [render-metadata items onclick-handler-fn]
  (cmp/card
    {:className "Swarmpit-form-card"}
    (cmp/card-header
      {:className "Swarmpit-form-card-header"
       :title     "dsdsd"
       :subheader "Running: 3, Updating: 5"})
    (cmp/card-content
      {:className "Swarmpit-table-card-content"}
      (list-raw render-metadata items onclick-handler-fn))))

(rum/defc responsive < rum/reactive
  [render-metadata items onclick-handler-fn]
  (html
    [:div
     (cmp/hidden
       {:only           ["xs" "sm" "md"]
        :implementation "js"}
       (table (:table render-metadata) items onclick-handler-fn))
     (cmp/hidden
       {:only           ["lg" "xl"]
        :implementation "js"}
       (list (:list render-metadata) items onclick-handler-fn))]))

(rum/defc responsive-raw < rum/reactive
  [render-metadata items onclick-handler-fn]
  (html
    [:div
     (cmp/hidden
       {:only           ["xs" "sm" "md"]
        :implementation "js"}
       (table-raw (:table render-metadata) items onclick-handler-fn))
     (cmp/hidden
       {:only           ["lg" "xl"]
        :implementation "js"}
       (list-raw (:list render-metadata) items onclick-handler-fn))]))