import { useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { completePasswordReset } from '../../api/auth';
import { Alert } from '../../shared/ui/Alert';
import { Card } from '../../shared/ui/Card';
import { Spinner } from '../../shared/ui/Spinner';
import { TextField } from '../../shared/ui/TextField';

export function ResetPasswordPage() {
    const [searchParams] = useSearchParams();
    const [email, setEmail] = useState(searchParams.get('email') || '');
    const [token, setToken] = useState(searchParams.get('token') || '');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<string | null>(null);

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        setSuccess(null);

        if (!email.trim() || !token.trim() || !newPassword) {
            setError('Email, token, and new password are required');
            return;
        }
        if (newPassword.length < 8) {
            setError('Password must be at least 8 characters');
            return;
        }
        if (newPassword !== confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        setLoading(true);
        try {
            const response = await completePasswordReset(email.trim(), token.trim(), newPassword);
            setSuccess(response.message);
            setNewPassword('');
            setConfirmPassword('');
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Could not reset password');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-linear-to-b from-blue-50 to-white p-4">
            <Card>
                <div className="w-full max-w-sm p-8">
                    <h1 className="text-2xl font-bold text-slate-800 mb-2">Reset Password</h1>
                    <p className="text-sm text-slate-600 mb-6">Set a new password for your account.</p>

                    {error && <Alert tone="error" title="Error" message={error} />}
                    {success && <Alert tone="success" title="Password Updated" message={success} />}

                    <form onSubmit={onSubmit} className="mt-4 space-y-4">
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
                            label="Reset Token"
                            type="text"
                            value={token}
                            onChange={(e) => setToken(e.target.value)}
                            placeholder="paste token"
                            disabled={loading}
                            required
                        />

                        <TextField
                            label="New Password"
                            type="password"
                            value={newPassword}
                            onChange={(e) => setNewPassword(e.target.value)}
                            placeholder="Min 8 characters"
                            disabled={loading}
                            required
                        />

                        <TextField
                            label="Confirm New Password"
                            type="password"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            placeholder="Repeat password"
                            disabled={loading}
                            required
                        />

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full bg-green-600 hover:bg-green-700 text-white py-2 px-4 rounded font-semibold disabled:opacity-50"
                        >
                            {loading ? <Spinner label="Updating..." /> : 'Update Password'}
                        </button>
                    </form>

                    <div className="mt-6 text-sm text-center">
                        <Link to="/" className="text-green-600 hover:text-green-700 font-semibold">
                            Back to Login
                        </Link>
                    </div>
                </div>
            </Card>
        </div>
    );
}
