/** @type {import('ts-jest').JestConfigWithTsJest} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom', 
  transform: {
    '^.+\\.(ts|tsx)$': 'ts-jest',
  },
  moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx', 'json', 'node'],
  // Handle the TypeScript files in resources
  transformIgnorePatterns: [
    "/node_modules/",
    // Add any specific paths to ignore here
  ],
  // Create a mock for fetch since it's not available in Node.js
  setupFiles: ['<rootDir>/jest.setup.js'],
  // Improve timer handling
  testTimeout: 10000, 
  fakeTimers: {
    enableGlobally: true,
  },
};
