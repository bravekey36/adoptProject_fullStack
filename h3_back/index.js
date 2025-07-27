const express = require('express');
const app = express();
const port = 3001; // 원하는 포트로 변경 가능

app.get('/', (req, res) => {
  res.send('Hello from h3_back backend!');
});

app.listen(port, () => {
  console.log(`Server is running on http://localhost:${port}`);
});