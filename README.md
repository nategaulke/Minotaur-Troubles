# Problem 1

## Overall Strategy

This problem is similar to the prisoner and light switch problem we saw earlier this semester. When it came to strategy I utilized a similar answer to that problem. The first guest will keep track of how many times that have eaten a cupcake in the maze. Every other guest who goes in the maze will ask for a new cupcake the first time they see the plate is empty, otherwise they will leave it in the state they found it. Once the first guest has had a cupcake n times, where n is equal to the number of guests, we can safely say that all guests have entered the maze and we can let our gracious host know that every has gone in the maze. We can say this because each time the plate is reset, it can only be done by a prisoner who has not seen the plate empty before. Each time the first guest has eaten the cupcake, we know that means that a new guest has been in the labyrinth.

## Experimentation

Unfortunately the way my threads work in my solution seems to have some sort of bug that I am unsure of how to trace. So while I know that this strategy works (as I was able to test it in a sequential manner), as it currently works, it works about 75% of the time. When it does work, the threads usually take about .500 seconds to all complete.

# Problem 2

## Overall Strategy

I used strategy 3 for the this problem. Strategy 1 would have an isssue with deadlocking, with multiple threads converging to the room at the same time, possible all of the guests. Strategy 2 is more viable and is reminsicient of the Alice and Bob protocol mentioned earlier this semester. However, there is also a risk of starvation where the room is available and no one is using the resource. THus strategy three, where threads "line up" to use the resource is the best strategy since it ensures that each thread will have access to the resource in a timely manner if they want to use it.

## Experimentation

I tested most frequently using 100 guests. On average the time that it took for all guests to have had a chance to look at the vase and then decide that they were done looking was 2.67 seconds, with 1.254 seconds as the low time and 5.950 seconds as the high time.
