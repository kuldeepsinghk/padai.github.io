/**
 * Test Helpers for Math Quiz Application
 * These utilities help with testing the application
 */

// Immediately define EventLogger in the global scope
(function(global) {
  'use strict';

  // Create the EventLogger object
  global.EventLogger = {
    // Recorded events storage
    events: {},
    
    /**
     * Listen for a specific custom event and log it
     * @param {string} eventName - Name of the event to listen for
     * @param {boolean} persistent - Whether to keep listening after first event
     * @return {Object} - This object for chaining
     */
    listen: function(eventName, persistent = true) {
      if (!this.events[eventName]) {
        this.events[eventName] = [];
      }
      
      const handler = function(event) {
        console.log(`Event "${eventName}" fired:`, event.detail);
        global.EventLogger.events[eventName].push({
          timestamp: new Date(),
          detail: event.detail
        });
        
        if (!persistent) {
          document.removeEventListener(eventName, handler);
        }
      };
      
      document.addEventListener(eventName, handler);
      console.log(`Now listening for "${eventName}" events`);
      
      return this;
    },
    
    /**
     * Listen for multiple events at once
     * @param {string[]} eventNames - Array of event names to listen for
     * @param {boolean} persistent - Whether to keep listening after first event
     * @return {Object} - This object for chaining
     */
    listenMultiple: function(eventNames, persistent = true) {
      for (let i = 0; i < eventNames.length; i++) {
        this.listen(eventNames[i], persistent);
      }
      return this;
    },
    
    /**
     * Clear all recorded events
     * @return {Object} - This object for chaining
     */
    clear: function() {
      const keys = Object.keys(this.events);
      for (let i = 0; i < keys.length; i++) {
        this.events[keys[i]] = [];
      }
      console.log("Cleared all recorded events");
      return this;
    },
    
    /**
     * Get events of a specific type
     * @param {string} eventName - Name of the event to get
     * @return {Array} - Array of recorded events
     */
    getEvents: function(eventName) {
      return this.events[eventName] || [];
    },
    
    /**
     * Start listening for all known quiz events
     * @return {Object} - This object for chaining
     */
    listenAll: function() {
      return this.listenMultiple([
        'quiz:question-loaded', 
        'quiz:answer-checked', 
        'quiz:completed',
        'quiz:data-loaded',
        'quiz:data-error'
      ]);
    }
  };
  
  // Log that the EventLogger is ready
  console.log('EventLogger initialized and ready to use');
  
})(typeof window !== 'undefined' ? window : this);

// Make available for testing environments (Node.js)
if (typeof module !== 'undefined' && typeof module.exports !== 'undefined') {
  module.exports = { EventLogger: window.EventLogger };
}
