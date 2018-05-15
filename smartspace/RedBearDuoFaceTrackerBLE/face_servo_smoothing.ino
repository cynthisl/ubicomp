// ino file for face servo smoothing
// not a class because just keeping variables in this file

#define FACE_WINDOW_SIZE 5

int servo_smoothing_vals[FACE_WINDOW_SIZE] = {(MAX_SERVO_ANGLE-MIN_SERVO_ANGLE)/2 };
int servo_smoothing_idx = 0;
int servo_smoothing_sum = 0;

int smoothServo(int new_val) {

  servo_smoothing_sum -= servo_smoothing_vals[servo_smoothing_idx];
  
  servo_smoothing_vals[servo_smoothing_idx++] = new_val;
  if(servo_smoothing_idx >= FACE_WINDOW_SIZE) {
    servo_smoothing_idx = 0;
  }

  servo_smoothing_sum += new_val;

  return (int)((float)servo_smoothing_sum/FACE_WINDOW_SIZE);
};

