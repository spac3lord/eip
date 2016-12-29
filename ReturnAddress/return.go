// Example implementations for Enterprise Integration Patterns
// www.EnterpriseIntegrationPatterns.com
//
// Simple example of ReturnAddress in Go

package ReturnAddress

import "fmt"

type Request struct {
    data        []int
    resultChan  chan int
}

func sum(a []int) (s int) {
  for _, v := range a {
    s += v
  }
  return
}

func handle(queue chan *Request) {
  for req := range queue {
    req.resultChan <- sum(req.data)
  }
}

func main() {
  reqChannel := make(chan *Request, 10)
  go handle(reqChannel)
  // Make two requests with separate return channels
  request1 := &Request{[]int{3, 4, 5}, make(chan int)}
  request2 := &Request{[]int{1, 2, 3}, make(chan int)}
  // Receive both results
  reqChannel <- request1
  reqChannel <- request2
  fmt.Printf("answer: %d %d\n", <-request1.resultChan, <-request2.resultChan)
}
