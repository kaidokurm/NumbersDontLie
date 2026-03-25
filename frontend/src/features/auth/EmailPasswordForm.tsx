import { useState } from 'react';
import { Link } from 'react-router-dom';
import { TextField } from '../../shared/ui/TextField';
import { Button } from '../../shared/ui/Button';
import { Alert } from '../../shared/ui/Alert';
import { Spinner } from '../../shared/ui/Spinner';
import { registerUser, loginUser, resendVerificationCode } from '../../api/auth';
import { useLocalAuth } from '../../shared/auth/useLocalAuth';

export interface EmailPasswordFormProps {
    mode: 'login' | 'register';
    onSuccess: () => void;
    onSwitchMode: () => void;
}

export function EmailPasswordForm({ mode, onSuccess, onSwitchMode }: EmailPasswordFormProps) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [twoFactorCode, setTwoFactorCode] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);
    const [resendLoading, setResendLoading] = useState(false);
    const [resendMessage, setResendMessage] = useState<string | null>(null);
    const { login } = useLocalAuth();

    const isRegister = mode === 'register';

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);
        setResendMessage(null);

        // Validate inputs
        if (!email || !password) {
            setError('Email and password are required');
            return;
        }

        if (isRegister && password !== confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        if (isRegister && password.length < 8) {
            setError('Password must be at least 8 characters');
            return;
        }

        setLoading(true);

        try {
            if (isRegister) {
                // Register
                const response = await registerUser(email, password);
                setSuccess(`Registration successful! Check your email at ${response.email} for verification code.`);
                setEmail('');
                setPassword('');
                setConfirmPassword('');
                // After success, could auto-switch to login mode or show verification form
                setTimeout(() => onSwitchMode(), 2000);
            } else {
                // Login
                const response = await loginUser(email, password, twoFactorCode || undefined);
                // Store tokens and update auth state
                login(response.accessToken, response.refreshToken);
                // Trigger success callback to close modal
                onSuccess();
            }
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An error occurred');
        } finally {
            setLoading(false);
        }
    };

    const canResendVerification = !isRegister && !!email.trim() && !!error && error.toLowerCase().includes('verification');

    const handleResendVerification = async () => {
        if (!email.trim()) {
            setError('Enter your email first to resend verification code');
            return;
        }
        setResendLoading(true);
        setResendMessage(null);
        try {
            const response = await resendVerificationCode(email.trim());
            setResendMessage(response.message);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Could not resend verification code');
        } finally {
            setResendLoading(false);
        }
    };

    return (
        <div className="w-full max-w-sm">
            <h2 className="text-2xl font-bold mb-6 text-center text-slate-800">
                {isRegister ? 'Create Account' : 'Sign In'}
            </h2>

            {error && <Alert tone="error" title="Error" message={error} />}
            {success && <Alert tone="success" title="Success" message={success} />}
            {resendMessage && <Alert tone="success" title="Verification Code Sent" message={resendMessage} />}

            <form onSubmit={handleSubmit} className="flex flex-col gap-4">
                <TextField
                    label="Email"
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@example.com"
                    disabled={loading}
                    required
                />

                <TextField
                    label="Password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder={isRegister ? 'Min 8 characters' : '••••••••'}
                    disabled={loading}
                    required
                />

                {!isRegister && (
                    <TextField
                        label="2FA Code (if enabled)"
                        type="text"
                        value={twoFactorCode}
                        onChange={(e) => setTwoFactorCode(e.target.value)}
                        placeholder="123456"
                        disabled={loading}
                        maxLength={6}
                    />
                )}

                {!isRegister && (
                    <div className="text-right -mt-1">
                        <Link to="/forgot-password" className="text-sm text-green-600 hover:text-green-700 font-semibold">
                            Forgot password?
                        </Link>
                    </div>
                )}

                {canResendVerification && (
                    <button
                        type="button"
                        onClick={handleResendVerification}
                        disabled={resendLoading}
                        className="text-sm text-blue-700 hover:text-blue-800 font-semibold disabled:opacity-50"
                    >
                        {resendLoading ? 'Resending...' : 'Resend verification code'}
                    </button>
                )}

                {isRegister && (
                    <TextField
                        label="Confirm Password"
                        type="password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        placeholder="••••••••"
                        disabled={loading}
                        required
                    />
                )}

                <Button
                    type="submit"
                    disabled={loading}
                    fullWidth
                    className="bg-green-600 hover:bg-green-700 text-white py-2 px-4 rounded font-semibold mt-2"
                >
                    {loading ? <Spinner label="Loading..." /> : isRegister ? 'Create Account' : 'Sign In'}
                </Button>
            </form>

            <div className="mt-6 text-center text-slate-600 text-sm">
                {isRegister ? (
                    <>
                        Already have an account?{' '}
                        <button onClick={onSwitchMode} className="text-green-600 hover:text-green-700 font-semibold">
                            Sign In
                        </button>
                    </>
                ) : (
                    <>
                        Don't have an account?{' '}
                        <button onClick={onSwitchMode} className="text-green-600 hover:text-green-700 font-semibold">
                            Create Account
                        </button>
                    </>
                )}
            </div>
        </div>
    );
}
