// Dev server on port 3000 (the API server uses 8080), reachable from other devices on the network.
config.devServer = Object.assign({}, config.devServer, {
    host: "0.0.0.0",
    port: 3000,
    allowedHosts: "all"
});
