/* 
The acceleration equations for the 2D three-body problem (see equations 18 through 21):

  d^2[x1]/dt^2 = G*m2*(x2 - x1) / alpha

  d^2[y1]/dt^2 = G*m2*(y2 - y1) / alpha

  d^2[x2]/dt^2 = G*m1*(x1 - x2) / alpha

  d^2[y2]/dt^2 = G*m1*(y1 - y2) / alpha

where: 

  G = gravitational constant = 6.6725985 X 10^(-11) N-m^2/kg^2

  alpha = [ (x2 - x1)^2 + (y2 - y1)^2 ]^(3/2)  AND  alpha <> 0 (if alpha = 0, we have an infinite acceleration which is not physically possible)
*/

var N = 2; // The number of bodies (point masses) this code is designed to handle.
var G = 6.67384E-11; // Big-G, in N(m/kg)^2.
var h = 0.000001; // Interval between time steps, in seconds. The smaller the value the more accurate the simulation. This value was empirically derived by visually observing the simulation over time.
var iterationsPerFrame = 400; // The number of calculations made per animation frame, this is an empirically derived number based on the value of h.
    
var m1;
var m1_half; // Initially, will contain a copy of m1.
var m2;
var m2_half; 

self.onmessage = function (evt) { // evt.data contains the data passed from the calling main page thread.
  switch (evt.data.cmd) {
    case 'init':
      init(evt.data.initialConditions); // Transfer the initial conditions data to the persistant variables in this thread.
      break;
    case 'crunch':
      crunch();
      break;
    default:
      console.error("ERROR FROM worker.js: SWITCH STATEMENT ERROR IN self.onmessage");
  } // switch
};

// The denominator alpha for the acceleration equations 18 through 21:
function alpha(m1, m2) {
  var delta_x = m2.p.x - m1.p.x;
  var delta_y = m2.p.y - m1.p.y;

  var delta_x_squared = delta_x * delta_x;
  var delta_y_squared = delta_y * delta_y;

  var base = delta_x_squared + delta_y_squared;

  return Math.sqrt(base * base * base); // Raise the base to the 3/2 power so as to calculate (x_2 - x_1 )^2 + (y_2 - y_1 )^2]^(3/2)
}

this.init = function (initialConditions) {

  // Define local mass object constructor function:
  function Mass(initialCondition) {
    this.m = initialCondition.mass; // The mass of the point mass.
    this.p = { x: initialCondition.position.x, y: initialCondition.position.y }; // The position of the mass.
    this.v = { x: initialCondition.velocity.x, y: initialCondition.velocity.y }; // The x- and y-components of velocity for the mass.
    this.a = {}; // Will contain the x- and y-components of acceleration for the mass.
  }

  if (initialConditions.length != N) {
    console.error("ERROR FROM worker.js: THE initialConditions ARRAY DOES NOT CONTAIN EXACTLY " + N + " OBJECTS - init() TERMINATED");
    return;
  }

  // Set the local mass object global variables:
  m1 = new Mass(initialConditions[0]);
  m1_half = new Mass(initialConditions[0]); // Create a copy of m1.
  m2 = new Mass(initialConditions[1]);
  m2_half = new Mass(initialConditions[1]); 

  // Calculate initial acceleration values (using initial conditions) in preparation for using equation 25:
  m1.a.x = G * m2.m * (m2.p.x - m1.p.x) / alpha(m1, m2); // Equation 18.
  m1.a.y = G * m2.m * (m2.p.y - m1.p.y) / alpha(m1, m2); // Equation 19.
  m2.a.x = G * m1.m * (m1.p.x - m2.p.x) / alpha(m1, m2); // Equation 20.
  m2.a.y = G * m1.m * (m1.p.y - m2.p.y) / alpha(m1, m2); // Equation 21.

  function equation25(x, v, a) {
    return x + 0.5 * h * v + 0.25 * (h * h) * a;  // Equation 25.
  }

  // For the first iteration (and only the first iteration), use equation 25 (instead of equation 22) to calculate the initial half-integer position values:
  m1_half.p.x = equation25(m1.p.x, m1.v.x, m1.a.x);
  m1_half.p.y = equation25(m1.p.y, m1.v.y, m1.a.y);
  m2_half.p.x = equation25(m2.p.x, m2.v.x, m2.a.x);
  m2_half.p.y = equation25(m2.p.y, m2.v.y, m2.a.y);
} // this.init


this.crunch = function () {
  for (var i = 0; i < iterationsPerFrame; i++) {
    // Calculate half-integer acceleration values (using equations 18 through 21) in preparation for using equation 23:
    m1_half.a.x = G * m2_half.m * (m2_half.p.x - m1_half.p.x) / alpha(m1_half, m2_half); // Equation 18.
    m1_half.a.y = G * m2_half.m * (m2_half.p.y - m1_half.p.y) / alpha(m1_half, m2_half); // Equation 19.
    m2_half.a.x = G * m1_half.m * (m1_half.p.x - m2_half.p.x) / alpha(m1_half, m2_half); // Equation 20.
    m2_half.a.y = G * m1_half.m * (m1_half.p.y - m2_half.p.y) / alpha(m1_half, m2_half); // Equation 21.

    // Calculate velocity values using equation 23:
    m1.v.x = equation23(m1.v.x, m1_half.a.x);
    m1.v.y = equation23(m1.v.y, m1_half.a.y);
    m2.v.x = equation23(m2.v.x, m2_half.a.x);
    m2.v.y = equation23(m2.v.y, m2_half.a.y);

    // Calculate position values using equation 24:
    m1.p.x = equation24(m1_half.p.x, m1.v.x);
    m1.p.y = equation24(m1_half.p.y, m1.v.y);
    m2.p.x = equation24(m2_half.p.x, m2.v.x);
    m2.p.y = equation24(m2_half.p.y, m2.v.y);

    // Calculate half-integer position values using equation 22:
    m1_half.p.x = equation22(m1.p.x, m1.v.x);
    m1_half.p.y = equation22(m1.p.y, m1.v.y);
    m2_half.p.x = equation22(m2.p.x, m2.v.x);
    m2_half.p.y = equation22(m2.p.y, m2.v.y);
  } // for

  self.postMessage([m1, m2]); // Send the crunched data back to the UI thread to be rendered onscreen.

  function equation23(v, a) {
    return v + h * a; // Equation 23.
  }

  function equation24(x, v) {
    return x + 0.5 * h * v; // Equation 24.
  }

  function equation22(x, v) {
    return x + 0.5 * h * v; // Equation 22, this function is of course the same as the equation24(x, v) function.
  }
} // this.crunch