// Mock fetch API
global.fetch = jest.fn();

// Setup DOM globals for browser environment testing
global.document = {
  createElement: jest.fn(() => ({
    classList: {
      add: jest.fn()
    },
    style: {},
    remove: jest.fn()
  })),
  body: {
    appendChild: jest.fn()
  }
};

// Don't mock setTimeout directly as Jest handles this with fake timers
// global.setTimeout = jest.fn((callback) => callback());

// Mock window object if needed
global.window = {};

// Setup console mocks to avoid cluttering test output
global.console.log = jest.fn();
global.console.error = jest.fn();
